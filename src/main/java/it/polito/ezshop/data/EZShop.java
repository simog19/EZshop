package it.polito.ezshop.data;

import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.model.*;

import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.*;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class EZShop implements EZShopInterface {


    @Override
    public void reset() {

        for (it.polito.ezshop.model.User u : DataManager.getInstance().getUsers()) {
            DataManager.getInstance().deleteUser(u);
        }

        for (it.polito.ezshop.model.Customer u : DataManager.getInstance().getCustomers()) {
            DataManager.getInstance().deleteCustomer(u);
        }

        for (LoyaltyCard u : DataManager.getInstance().getLoyaltyCards()) {
            DataManager.getInstance().deleteLoyaltyCard(u);
        }

        for (it.polito.ezshop.model.ProductType u : DataManager.getInstance().getProductTypes()) {
            DataManager.getInstance().deleteProductType(u);
        }

        for (Position u : DataManager.getInstance().getPositions()) {
            DataManager.getInstance().deletePosition(u);
        }

        for (it.polito.ezshop.model.Order u : DataManager.getInstance().getOrders()) {
            DataManager.getInstance().deleteOrder(u);
        }

        for (Sale u : DataManager.getInstance().getSales()) {
            DataManager.getInstance().deleteSale(u);
        }

        for (CReturn u : DataManager.getInstance().getReturns()) {
            DataManager.getInstance().deleteReturn(u);
        }

        for (DummyCredit u : DataManager.getInstance().getDummyCredits()) {
            DataManager.getInstance().deleteDummyCredit(u);
        }

        for (DummyDebit u : DataManager.getInstance().getDummyDebits()) {
            DataManager.getInstance().deleteDummyDebit(u);
        }

        for (BalanceTransaction u : DataManager.getInstance().getBalanceTransactions()) {
            DataManager.getInstance().deleteBalanceTransaction(u);
        }

        for (Product p : DataManager.getInstance().getProducts()) {
            DataManager.getInstance().deleteProduct(p);
        }

    }

    @Override
    public Integer createUser(String username, String password, String role) throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException {
        
        if (role == null || role.isEmpty() || !(role.equals("Administrator") || role.equals("Cashier") || role.equals("ShopManager"))) {
            throw new InvalidRoleException();
        }

        if (username == null || username.isEmpty()) {
            throw new InvalidUsernameException();
        }

        if (password == null || password.isEmpty()) {
            throw new InvalidPasswordException();
        }

        boolean usernameExists = DataManager.getInstance()
            .getUsers()
            .stream()
            .anyMatch(user -> user.getUsername().equals(username));

        if (usernameExists) {
            return -1;
        }

        OptionalInt maxId = DataManager.getInstance()
            .getUsers()
            .stream()
            .mapToInt(it.polito.ezshop.model.User::getId)
            .max();

        int newId = !maxId.isPresent() ? 1 : (maxId.getAsInt() + 1);
        if (!DataManager.getInstance().insertUser(new it.polito.ezshop.model.User(newId, username, password, role))) {
            return -1;
        }

        return newId;
    }

    @Override
    public boolean deleteUser(Integer id) throws InvalidUserIdException, UnauthorizedException {
        
        if (id == null || id <= 0) throw new InvalidUserIdException();

        if (!RightsManager.getInstance().canManageUsers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        Optional<it.polito.ezshop.model.User> optUser = DataManager.getInstance()
            .getUsers()
            .stream()
            .filter(u -> u.getId().equals(id))
            .findFirst();

        if (!optUser.isPresent()) return false; 
        
        return DataManager.getInstance().deleteUser(optUser.get());
    }

    @Override
    public List<User> getAllUsers() throws UnauthorizedException {

        if (!RightsManager.getInstance().canManageUsers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        return DataManager.getInstance()
            .getUsers()
            .stream()
            .map(u -> (User)u)
            .collect(toList());
    }

    @Override
    public User getUser(Integer id) throws InvalidUserIdException, UnauthorizedException {
        
        if (id == null || id <= 0) throw new InvalidUserIdException();

        if (!RightsManager.getInstance().canManageUsers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        Optional<it.polito.ezshop.model.User> optUser = DataManager.getInstance()
            .getUsers()
            .stream()
            .filter(u -> u.getId().equals(id))
            .findFirst();

        return optUser.orElse(null);
    }

    @Override
    public boolean updateUserRights(Integer id, String role) throws InvalidUserIdException, InvalidRoleException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageUsers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (id == null || id <= 0) throw new InvalidUserIdException();

        if (role == null || role.isEmpty() || !(role.equals("Administrator") || role.equals("Cashier") || role.equals("ShopManager"))) {
            throw new InvalidRoleException();
        }

        Optional<it.polito.ezshop.model.User> optUser = DataManager.getInstance()
            .getUsers()
            .stream()
            .filter(u -> u.getId().equals(id))
            .findFirst();

        optUser.ifPresent(u -> {
            u.setRole(role);
            DataManager.getInstance().updateUser(u);
        });

        return optUser.isPresent();       
    }

    @Override
    public User login(String username, String password) throws InvalidUsernameException, InvalidPasswordException {
        
        if (username == null || username.isEmpty()) {
            throw new InvalidUsernameException();
        }

        if (password == null || password.isEmpty()) {
            throw new InvalidPasswordException();
        }

        if (!LoginManager.getInstance().tryLogin(username, password)) {
            return null;
        }

        return (User)LoginManager.getInstance().getLoggedUser();
    }

    @Override
    public boolean logout() {
        return LoginManager.getInstance().tryLogout();
    }

    @Override
    public Integer createProductType(String description, String productCode, double pricePerUnit, String note) throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }
        
        if (description == null || description.isEmpty()) {
            throw new InvalidProductDescriptionException();
        }

        if (productCode == null || productCode.isEmpty() || !isValidBarcode(productCode)) {
            throw new InvalidProductCodeException();
        }

        if (pricePerUnit <= 0.0) {
            throw new InvalidPricePerUnitException();
        }

        if (DataManager.getInstance().getProductTypes().stream().anyMatch(p -> p.getBarCode().equals(productCode))) {
            return -1;
        }

        OptionalInt maxId = DataManager.getInstance()
            .getProductTypes()
            .stream()
            .mapToInt(it.polito.ezshop.model.ProductType::getId)
            .max();

        int newId = !maxId.isPresent() ? 1 : (maxId.getAsInt() + 1);
        it.polito.ezshop.model.ProductType newProd = new it.polito.ezshop.model.ProductType(newId, productCode, description, pricePerUnit, 0, 0.0, note, null);
        if (!DataManager.getInstance().insertProductType(newProd)) {
            return -1;
        }

        return newId;
    }

    @Override
    public boolean updateProduct(Integer id, String newDescription, String newCode, double newPrice, String newNote) throws InvalidProductIdException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (id == null || id <= 0) {
            throw new InvalidProductIdException();
        }

        if (newDescription == null || newDescription.isEmpty()) {
            throw new InvalidProductDescriptionException();
        }

        if (newCode == null || newCode.isEmpty() || !isValidBarcode(newCode)) {
            throw new InvalidProductCodeException();
        }

        if (newPrice <= 0.0) {
            throw new InvalidPricePerUnitException();
        }

        Optional<it.polito.ezshop.model.ProductType> prod = DataManager.getInstance()
            .getProductTypes()
            .stream()
            .filter(p -> p.getId() == id)
            .findFirst();

        if (!prod.isPresent()) return false;

        boolean isNewCodeAlreadyUsed = DataManager.getInstance()
            .getProductTypes()
            .stream()
            .filter(p -> !p.equals(prod.get()))
            .anyMatch(p -> p.getBarCode().equals(newCode));
            

        if (isNewCodeAlreadyUsed) return false;

        prod.get().setBarCode(newCode);
        prod.get().setProductDescription(newDescription);
        prod.get().setPricePerUnit(newPrice);
        prod.get().setNote(newNote);

        return DataManager.getInstance().updateProductType(prod.get());
    }

    @Override
    public boolean deleteProductType(Integer id) throws InvalidProductIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (id == null || id <= 0) {
            throw new InvalidProductIdException();
        }

        Optional<it.polito.ezshop.model.ProductType> prod = DataManager.getInstance()
            .getProductTypes()
            .stream()
            .filter(p -> p.getId() == id)
            .findFirst();

        if (!(prod.isPresent())) return false;

        return DataManager.getInstance().deleteProductType(prod.get());
    }

    @Override
    public List<ProductType> getAllProductTypes() throws UnauthorizedException {

        if (!RightsManager.getInstance().canListAllProductTypes(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        return DataManager.getInstance()
            .getProductTypes()
            .stream()
            .collect(toList());
    }

    @Override
    public ProductType getProductTypeByBarCode(String barCode) throws InvalidProductCodeException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (barCode == null || barCode.isEmpty() || !isValidBarcode(barCode)) {
            throw new InvalidProductCodeException();
        }

        return DataManager.getInstance()
            .getProductTypes()
            .stream()
            .filter(p -> p.getBarCode().equals(barCode))
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<ProductType> getProductTypesByDescription(String description) throws UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        return DataManager.getInstance()
            .getProductTypes()
            .stream()
            .filter(p -> p.getProductDescription().contains(description == null ? "" : description))
            .collect(toList());
    }

    @Override
    public boolean updateQuantity(Integer productId, int toBeAdded) throws InvalidProductIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (productId == null || productId <= 0) {
            throw new InvalidProductIdException();
        }

        Optional<it.polito.ezshop.model.ProductType> prod = DataManager.getInstance()
            .getProductTypes()
            .stream()
            .filter(p -> p.getId() == productId)
            .findFirst();

        if (!prod.isPresent()) return false;

        if (prod.get().getQuantity() + toBeAdded < 0) return false;

        if (prod.get().getLocation() == "") return false;

        prod.get().addQuantityOffset(toBeAdded);
        return DataManager.getInstance().updateProductType(prod.get());
    }

    @Override
    public boolean updatePosition(Integer productId, String newPos) throws InvalidProductIdException, InvalidLocationException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (productId == null || productId <= 0) {
            throw new InvalidProductIdException();
        }

        Optional<it.polito.ezshop.model.ProductType> prod = DataManager.getInstance()
            .getProductTypes()
            .stream()
            .filter(p -> p.getId() == productId)
            .findFirst();

        if (newPos == null || newPos.isEmpty()) {
            
            Position oldpos = prod.get().getAssignedPosition();
            prod.get().setLocation("");

            DataManager.getInstance().updateProductType(prod.get());
            DataManager.getInstance().updatePosition(oldpos);

            return true;
        }

        if (!(newPos.matches("[1-9][0-9]*-[a-zA-Z]+-[1-9][0-9]*"))) {
            throw new InvalidLocationException();
        }

        Optional<Position> pos = DataManager.getInstance()
            .getPositions()
            .stream()
            .filter(ps -> ps.toString().equals(newPos))
            .findFirst();
        
        String[] pieces = newPos.split("-");
        Position actualPos = !(pos.isPresent()) ? new Position(Integer.valueOf(pieces[0]),pieces[1],Integer.valueOf(pieces[2]), null) : pos.get();

        if (!(pos.isPresent())) {
            DataManager.getInstance().insertPosition(actualPos);
        }

        if (!prod.get().assingToPosition(actualPos)) {
            return false;
        }

        DataManager.getInstance().updatePosition(actualPos);
        return DataManager.getInstance().updateProductType(prod.get());
    }

    @Override
    public Integer issueOrder(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (productCode == null ||  productCode.isEmpty() || !isValidBarcode(productCode)) {
            throw new InvalidProductCodeException();
        }

        if (quantity <= 0){
            throw new InvalidQuantityException();
        }

        if (pricePerUnit <= 0){
            throw new InvalidPricePerUnitException();
        }

        Optional<it.polito.ezshop.model.ProductType> prod = DataManager.getInstance()
                .getProductTypes()
                .stream()
                .filter(p -> p.getBarCode().equals(productCode))
                .findFirst();

        if(!(prod.isPresent())) {
            return -1;
        }

        OptionalInt maxId = DataManager.getInstance()
                .getOrders()
                .stream()
                .mapToInt(it.polito.ezshop.model.Order::getOrderId)
                .max();
        int newId = !maxId.isPresent() ? 1 : (maxId.getAsInt() + 1);

        it.polito.ezshop.model.Order newOrder = new it.polito.ezshop.model.Order(newId, pricePerUnit, quantity, prod.get(), EOrderStatus.ISSUED);
        if(!DataManager.getInstance().insertOrder(newOrder)){
            return -1;
        }
        return newId;
    }

    @Override
    public Integer payOrderFor(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (productCode == null ||  productCode.isEmpty() || !isValidBarcode(productCode)) {
            throw new InvalidProductCodeException();
        }

        if (quantity <= 0){
            throw new InvalidQuantityException();
        }

        if (pricePerUnit <= 0){
            throw new InvalidPricePerUnitException();
        }

        if ((quantity*pricePerUnit > this.computeBalance())){
            return -1;
        }

        Optional<it.polito.ezshop.model.ProductType> prod = DataManager.getInstance()
                .getProductTypes()
                .stream()
                .filter(p -> p.getBarCode() == productCode)
                .findFirst();

        if(!(prod.isPresent())) {
            return -1;
        }

        OptionalInt maxOrdId = DataManager.getInstance()
                .getOrders()
                .stream()
                .mapToInt(it.polito.ezshop.model.Order::getOrderId)
                .max();
        int newOrdId = !maxOrdId.isPresent() ? 1 : (maxOrdId.getAsInt() + 1);

        it.polito.ezshop.model.Order newOrder = new it.polito.ezshop.model.Order(newOrdId, pricePerUnit, quantity, prod.get(), EOrderStatus.PAYED);
        if(!DataManager.getInstance().insertOrder(newOrder)){
            return -1;
        }

        OptionalInt maxBalId = DataManager.getInstance()
                .getBalanceTransactions()
                .stream()
                .mapToInt(it.polito.ezshop.model.BalanceTransaction::getBalanceId)
                .max();
        int newBalId = !maxBalId.isPresent() ? 1 : (maxBalId.getAsInt() + 1);

        newOrder.setBalanceId(newBalId);
        DebitTransaction newDebT = new DebitTransaction(newBalId, newOrder);
        if(!DataManager.getInstance().insertBalanceTransaction(newDebT)){
            return -1;
        }

        return newOrdId;
    }

    @Override
    public boolean payOrder(Integer orderId) throws InvalidOrderIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }
        
        if (orderId == null || orderId <= 0){
            throw new InvalidOrderIdException();
        }

        Optional<it.polito.ezshop.model.Order> ord = DataManager.getInstance()
                .getOrders()
                .stream()
                .filter(p -> p.getOrderId() == orderId)
                .findFirst();

        if (!(ord.isPresent())){
            return false;
        }

        if (!(ord.get().getStatus().equals(EOrderStatus.ISSUED.toString()) || ord.get().getStatus().equals(EOrderStatus.PAYED.toString()))){
            return false;
        }

        if (ord.get().getTotalValue() > computeBalance()) return false;

        ord.get().setAsPayed();

        OptionalInt maxBalId = DataManager.getInstance()
                .getBalanceTransactions()
                .stream()
                .mapToInt(it.polito.ezshop.model.BalanceTransaction::getBalanceId)
                .max();

        int newBalId = !maxBalId.isPresent() ? 1 : (maxBalId.getAsInt() + 1);

        ord.get().setBalanceId(newBalId);
        DebitTransaction newDebT = new DebitTransaction(newBalId, ord.get());
        if (!DataManager.getInstance().insertBalanceTransaction(newDebT)){
            return false;
        }

        return DataManager.getInstance().updateOrder(ord.get());
    }

    @Override
    public boolean recordOrderArrival(Integer orderId) throws InvalidOrderIdException, UnauthorizedException, InvalidLocationException {
        
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }
        
        if (orderId == null || orderId <= 0) {
            throw new InvalidOrderIdException();
        }

        Optional<it.polito.ezshop.model.Order> ord = DataManager.getInstance()
            .getOrders()
            .stream()
            .filter(p -> p.getOrderId() == orderId)
            .findFirst();

        if (!(ord.isPresent())){
            return false;
        }

        if (ord.get().getRelatedProduct().getAssignedPosition() == null) {
            throw new InvalidLocationException();
        }

        if (!(ord.get().getStatus().equals(EOrderStatus.PAYED.toString())) || ord.get().getStatus().equals(EOrderStatus.COMPLETED.toString())) {
            return false;
        }

        if (ord.get().getStatus().equals(EOrderStatus.PAYED.toString())) {
            ord.get().setAsCompleted();
            ord.get().getRelatedProduct().addQuantityOffset(ord.get().getQuantity());
            DataManager.getInstance().updateProductType(ord.get().getRelatedProduct());
        }

        return DataManager.getInstance().updateOrder(ord.get());
    }

    @Override
    public boolean recordOrderArrivalRFID(Integer orderId, String RFIDfrom) throws InvalidOrderIdException, UnauthorizedException, InvalidLocationException, InvalidRFIDException {
        
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (orderId == null || orderId <= 0) {
            throw new InvalidOrderIdException();
        }

        Optional<it.polito.ezshop.model.Order> ord = DataManager.getInstance()
            .getOrders()
            .stream()
            .filter(p -> p.getOrderId() == orderId)
            .findFirst();

        if (!ord.isPresent()) return false;

        if (ord.get().getRelatedProduct().getAssignedPosition() == null) {
            throw new InvalidLocationException();
        }

        if(!isValidRFID(RFIDfrom)) throw new InvalidRFIDException();

        List<String> allRFIDs = new ArrayList<>();
        for (int i = 0; i < ord.get().getQuantity(); i++) {
            allRFIDs.add(String.format("%012d", Long.parseLong(RFIDfrom) + i));
        }

        boolean anyDuplicate = DataManager.getInstance()
            .getProducts()
            .stream()
            .map(Product::getRFID)
            .anyMatch(rfid -> allRFIDs.contains(rfid));

        if (anyDuplicate) throw new InvalidRFIDException();

        if (!(ord.get().getStatus().equals(EOrderStatus.PAYED.toString())) || ord.get().getStatus().equals(EOrderStatus.COMPLETED.toString())) { //TODO: what should this if check?
            return false;
        }

        if (ord.get().getStatus().equals(EOrderStatus.PAYED.toString())) {
        
            allRFIDs.forEach(rfid -> DataManager.getInstance().insertProduct(new Product(rfid, ord.get().getRelatedProduct())));
            
            ord.get().setAsCompleted();
            ord.get().getRelatedProduct().addQuantityOffset(ord.get().getQuantity());
            DataManager.getInstance().updateProductType(ord.get().getRelatedProduct());
        }

        return DataManager.getInstance().updateOrder(ord.get()); 
    }

    @Override
    public List<Order> getAllOrders() throws UnauthorizedException {
        if (!RightsManager.getInstance().canManageProductsCatalogue(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        return DataManager.getInstance().getOrders().stream().collect(toList());
    }

    @Override
    public Integer defineCustomer(String customerName) throws InvalidCustomerNameException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageCustomers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (customerName == null || customerName.isEmpty()) {
            throw new InvalidCustomerNameException();
        }

        if (DataManager.getInstance().getCustomers().stream().anyMatch(c -> c.getCustomerName().equals(customerName))) {
            return -1;
        }

        OptionalInt maxId = DataManager.getInstance()
            .getCustomers()
            .stream()
            .mapToInt(it.polito.ezshop.model.Customer::getId)
            .max();

        int newId = !maxId.isPresent() ? 1 : (maxId.getAsInt() + 1);
        it.polito.ezshop.model.Customer newCustomer = new it.polito.ezshop.model.Customer(newId, customerName, null);
        if (!DataManager.getInstance().insertCustomer(newCustomer)) {
            return -1;
        }

        return newId;
    }

    @Override
    public boolean modifyCustomer(Integer id, String newCustomerName, String newCustomerCard) throws InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageCustomers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (id == null || id <= 0) {
            throw new InvalidCustomerIdException();
        }

        if (newCustomerName == null || newCustomerName.isEmpty()) {
            throw new InvalidCustomerNameException();
        }

        if (newCustomerCard != null && !newCustomerCard.isEmpty() && (newCustomerCard.length() != 10 || !newCustomerCard.chars().allMatch(ch -> ch >= '0' && ch <= '9'))) {  
            throw new InvalidCustomerCardException();
        }

        Optional<it.polito.ezshop.model.Customer> customer = DataManager.getInstance()
            .getCustomers()
            .stream()
            .filter(s -> s.getId() == id)
            .findFirst();
        
        if (!customer.isPresent()) return false;

        if (newCustomerCard !=null) {
            if (newCustomerCard.isEmpty()) { 

                LoyaltyCard card = customer.get().getLoyaltyCard();

                customer.get().setCustomerCard("");
                DataManager.getInstance().updateLoyaltyCard(card);
            } else {

                Optional<LoyaltyCard> card = DataManager.getInstance()
                    .getLoyaltyCards()
                    .stream()
                    .filter(c -> c.getID().equals(newCustomerCard))
                    .findFirst();
    
                if (!card.isPresent() || card.get().getCustomer() != null) return false;

                customer.get().setCustomerCard(newCustomerCard);
                DataManager.getInstance().updateLoyaltyCard(customer.get().getLoyaltyCard());
            }
        }

        customer.get().setCustomerName(newCustomerName);
        return DataManager.getInstance().updateCustomer(customer.get());
    }

    @Override
    public boolean deleteCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageCustomers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (id == null || id <= 0) {
            throw new InvalidCustomerIdException();
        }

        Optional<it.polito.ezshop.model.Customer> customer = DataManager.getInstance()
            .getCustomers()
            .stream()
            .filter(s -> s.getId() == id)
            .findFirst();

        if (!(customer.isPresent())) return false;

        return DataManager.getInstance().deleteCustomer(customer.get());
    }

    @Override
    public Customer getCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageCustomers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (id == null || id <= 0) {
            throw new InvalidCustomerIdException();
        }

        Optional<it.polito.ezshop.model.Customer> customer = DataManager.getInstance()
            .getCustomers()
            .stream()
            .filter(s -> s.getId() == id)
            .findFirst();

        return customer.orElse(null);
    }

    @Override
    public List<Customer> getAllCustomers() throws UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageCustomers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        return DataManager.getInstance()
            .getCustomers()
            .stream()
            .collect(toList());
    }

    @Override
    public String createCard() throws UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageCustomers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        OptionalInt maxId = DataManager.getInstance()
            .getLoyaltyCards()
            .stream()
            .mapToInt(c -> Integer.parseInt(c.getID()))
            .max();

        int newId = !maxId.isPresent() ? 1 : (maxId.getAsInt() + 1);
        String newIdAsStr = String.format("%010d", newId);
        LoyaltyCard newCard = new LoyaltyCard(newIdAsStr, 0, null);

        return DataManager.getInstance().insertLoyaltyCard(newCard) ? newIdAsStr : "";
    }

    @Override
    public boolean attachCardToCustomer(String customerCard, Integer customerId) throws InvalidCustomerIdException, InvalidCustomerCardException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageCustomers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (customerId == null || customerId <= 0) {
            throw new InvalidCustomerIdException();
        }

        if (customerCard == null || customerCard.isEmpty() || customerCard.length() != 10 || !customerCard.chars().allMatch(ch -> ch >= '0' && ch <= '9')) {
            throw new InvalidCustomerCardException();
        }

        Optional<it.polito.ezshop.model.Customer> customer = DataManager.getInstance()
            .getCustomers()
            .stream()
            .filter(s -> s.getId() == customerId)
            .findFirst();

        Optional<LoyaltyCard> card = DataManager.getInstance()
            .getLoyaltyCards()
            .stream()
            .filter(c -> c.getID().equals(customerCard))
            .findFirst();

        if (!(customer.isPresent()) || !(card.isPresent()) || card.get().getCustomer() != null) {
            return false;
        }

        customer.get().setCustomerCard(customerCard);

        return DataManager.getInstance().updateLoyaltyCard(card.get()) && DataManager.getInstance().updateCustomer(customer.get());
    }

    @Override
    public boolean modifyPointsOnCard(String customerCard, int pointsToBeAdded) throws InvalidCustomerCardException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageCustomers(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }
 
        if (customerCard == null || customerCard.isEmpty() || customerCard.length() != 10) {
            throw new InvalidCustomerCardException();
        } else if (!customerCard.chars().allMatch(ch -> ch >= '0' && ch <= '9')) {
            throw new InvalidCustomerCardException();
        }

        Optional<LoyaltyCard> card = DataManager.getInstance()
            .getLoyaltyCards()
            .stream()
            .filter(c -> c.getID().equals(customerCard))
            .findFirst();

        if (!card.isPresent() || (pointsToBeAdded < 0 && card.get().getPoints() < -pointsToBeAdded)) {
            return false;
        }

        card.get().addPoints(pointsToBeAdded);
        return DataManager.getInstance().updateLoyaltyCard(card.get());
    }

    @Override
    public Integer startSaleTransaction() throws UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        OptionalInt maxId = DataManager.getInstance()
            .getSales()
            .stream()
            .mapToInt(Sale::getTicketNumber)
            .max();

        int newId = !maxId.isPresent() ? 1 : (maxId.getAsInt() + 1);
        Sale newSale = new Sale(newId, 0.0, null);

        DataManager.getInstance().insertSale(newSale);
        return newId;
    }

    @Override
    public boolean addProductToSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException();
        }

        if (!isValidBarcode(productCode)) {
            throw new InvalidProductCodeException();
        }

        if (amount < 0) {
            throw new InvalidQuantityException();
        }

        Optional<it.polito.ezshop.model.ProductType> prod = DataManager.getInstance()
            .getProductTypes()
            .stream()
            .filter(p -> p.getBarCode().equals(productCode))
            .findFirst();

        if (!(prod.isPresent()) || prod.get().getQuantity() < amount) return false;

        Optional<Sale> sale = DataManager.getInstance()
            .getSales()
            .stream()
            .filter(s -> s.getTicketNumber() == transactionId)
            .findFirst();

        if (!sale.isPresent() || sale.get().isCommitted()) {
            return false;
        }

        sale.get().addProduct(prod.get(), amount);
        prod.get().addQuantityOffset(-amount);

        return DataManager.getInstance().updateSale(sale.get()) && DataManager.getInstance().updateProductType(prod.get());
    }

    @Override
    public boolean addProductToSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, InvalidQuantityException, UnauthorizedException{
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException();
        }

        if (!isValidRFID(RFID)) {
            throw new InvalidRFIDException();
        }

        Optional<Product> prod = DataManager.getInstance()
            .getProducts()
            .stream()
            .filter(p -> p.getRFID().equals(RFID))
            .findFirst();

        if (!prod.isPresent() || !prod.get().isAvailable()) return false;

        Optional<Sale> sale = DataManager.getInstance()
            .getSales()
            .stream()
            .filter(s -> s.getTicketNumber() == transactionId)
            .findFirst();

        if (!sale.isPresent() || sale.get().isCommitted()) return false;

        sale.get().addProductRFID(prod.get());
        prod.get().setAvailable(false);
        prod.get().getRelativeProductType().addQuantityOffset(-1);
        
        DataManager.getInstance().updateProductType(prod.get().getRelativeProductType());
        DataManager.getInstance().updateProduct(prod.get());
        DataManager.getInstance().updateSale(sale.get());

        return true;
    }
    
    @Override
    public boolean deleteProductFromSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException();
        }

        if (!isValidBarcode(productCode)) {
            throw new InvalidProductCodeException();
        }

        if (amount < 0) {
            throw new InvalidQuantityException();
        }

        Optional<it.polito.ezshop.model.ProductType> prod = DataManager.getInstance()
            .getProductTypes()
            .stream()
            .filter(p -> p.getBarCode().equals(productCode))
            .findFirst();

        if (!(prod.isPresent())) return false;

        Optional<Sale> sale = DataManager.getInstance()
            .getSales()
            .stream()
            .filter(s -> s.getTicketNumber() == transactionId)
            .findFirst();

        if (!sale.isPresent() || sale.get().isCommitted()) {
            return false;
        }

        sale.get().addProduct(prod.get(), -amount);
        prod.get().addQuantityOffset(amount);

        return DataManager.getInstance().updateSale(sale.get()) && DataManager.getInstance().updateProductType(prod.get());
    }

    @Override
    public boolean deleteProductFromSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, InvalidQuantityException, UnauthorizedException{
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException();
        }

        if (!isValidRFID(RFID)) {
            throw new InvalidRFIDException();
        }

        Optional<Product> prod = DataManager.getInstance()
            .getProducts()
            .stream()
            .filter(p -> p.getRFID().equals(RFID))
            .findFirst();

        if (!prod.isPresent() || prod.get().isAvailable()) return false;

        Optional<Sale> sale = DataManager.getInstance()
            .getSales()
            .stream()
            .filter(s -> s.getTicketNumber() == transactionId)
            .findFirst();

        if (!sale.isPresent() || sale.get().isCommitted() || !sale.get().getProducRFIDs().contains(prod.get())) {
            return false;
        }
        
        sale.get().deleteProductRFID(prod.get());
        prod.get().setAvailable(true);
        prod.get().getRelativeProductType().addQuantityOffset(+1);
        
        DataManager.getInstance().updateProductType(prod.get().getRelativeProductType());
        DataManager.getInstance().updateProduct(prod.get());
        DataManager.getInstance().updateSale(sale.get());

        return true;
    }

    @Override
    public boolean applyDiscountRateToProduct(Integer transactionId, String productCode, double discountRate) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException();
        }

        if (!isValidBarcode(productCode)) {
            throw new InvalidProductCodeException();
        }

        if (discountRate < 0.0 || discountRate >= 1.0) {
            throw new InvalidDiscountRateException();
        }

        Optional<it.polito.ezshop.model.ProductType> prod = DataManager.getInstance()
            .getProductTypes()
            .stream()
            .filter(p -> p.getBarCode().equals(productCode))
            .findFirst();

        if (!(prod.isPresent())) return false;

        Optional<Sale> sale = DataManager.getInstance()
            .getSales()
            .stream()
            .filter(s -> s.getTicketNumber() == transactionId)
            .findFirst();

        if (!sale.isPresent() || sale.get().isCommitted() || !sale.get().getProductsList().contains(prod.get()) ) {
            return false;
        }

        sale.get().applyDiscountRateToProductGroup(prod.get(), discountRate);

        return DataManager.getInstance().updateSale(sale.get());
    }

    @Override
    public boolean applyDiscountRateToSale(Integer transactionId, double discountRate) throws InvalidTransactionIdException, InvalidDiscountRateException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException();
        }

        if (discountRate < 0.0 || discountRate >= 1.0) {
            throw new InvalidDiscountRateException();
        }

        Optional<Sale> sale = DataManager.getInstance()
            .getSales()
            .stream()
            .filter(s -> s.getTicketNumber() == transactionId)
            .findFirst();

        if (!sale.isPresent() || sale.get().isCommitted()) {
            return false;
        }

        sale.get().setDiscountRate(discountRate);

        return DataManager.getInstance().updateSale(sale.get());

    }

    @Override
    public int computePointsForSale(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException();
        }

        Optional<Sale> sale = DataManager.getInstance()
            .getSales()
            .stream()
            .filter(s -> s.getTicketNumber() == transactionId)
            .findFirst();

        if (!sale.isPresent()) return -1;

        return (int)(sale.get().getOriginalSalePrice() / 10);
    }

    @Override
    public boolean endSaleTransaction(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException();
        }

        Optional<Sale> sale = DataManager.getInstance()
            .getSales()
            .stream()
            .filter(s -> s.getTicketNumber() == transactionId)
            .findFirst();

        if (!sale.isPresent() || sale.get().isCommitted()) return false;

        sale.get().setAsCommitted();
        return DataManager.getInstance().updateSale(sale.get());
    }

    @Override
    public boolean deleteSaleTransaction(Integer saleNumber) throws InvalidTransactionIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (saleNumber == null || saleNumber <= 0) {
            throw new InvalidTransactionIdException();
        }

        Optional<Sale> sale = DataManager.getInstance()
            .getSales()
            .stream()
            .filter(s -> s.getTicketNumber() == saleNumber)
            .findFirst();

        if (!sale.isPresent()) return false;

        boolean isPaid = DataManager.getInstance()
            .getBalanceTransactions()
            .stream()
            .filter(bt -> bt instanceof CreditTransaction)
            .map(bt -> (CreditTransaction)bt)
            .filter(ct -> ct.getRelatedCreditOperation() instanceof it.polito.ezshop.model.Sale)
            .filter(ct -> ((it.polito.ezshop.model.Sale)ct.getRelatedCreditOperation()).equals(sale.get()))
            .count() == 1;

        if (isPaid) return false;

        for (it.polito.ezshop.data.ProductType prod : sale.get().getProductsList()) {
            it.polito.ezshop.model.ProductType xProd = (it.polito.ezshop.model.ProductType)prod;

            xProd.addQuantityOffset(sale.get().getQuantityByProduct(xProd));
            DataManager.getInstance().updateProductType(xProd);
        }

        for (Product prod : sale.get().getProducRFIDs()) {
            prod.getRelativeProductType().addQuantityOffset(1);
            prod.setAvailable(true);

            DataManager.getInstance().updateProduct(prod);
        }

        return DataManager.getInstance().deleteSale(sale.get());
    }

    @Override
    public SaleTransaction getSaleTransaction(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException();
        }

        Optional<Sale> sale = DataManager.getInstance()
            .getSales()
            .stream()
            .filter(s -> s.getTicketNumber() == transactionId)
            .filter(s -> s.isCommitted())
            .findFirst();

        return sale.orElse(null);
    }

    @Override
    public Integer startReturnTransaction(Integer transactionId) throws /*InvalidTicketNumberException,*/InvalidTransactionIdException, UnauthorizedException {
    	 
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException();
        }
        
        Optional<Sale> sale = DataManager.getInstance()
                .getSales()
                .stream()
                .filter(s -> s.getTicketNumber().equals(transactionId))
                .findFirst();
        
        if (!sale.isPresent() || !sale.get().isCommitted()) return -1;

        boolean isPaid = DataManager.getInstance()
            .getBalanceTransactions()
            .stream()
            .filter(bt -> bt instanceof CreditTransaction)
            .map(bt -> (CreditTransaction)bt)
            .filter(ct -> ct.getRelatedCreditOperation() instanceof it.polito.ezshop.model.Sale)
            .filter(ct -> ((it.polito.ezshop.model.Sale)ct.getRelatedCreditOperation()).equals(sale.get()))
            .count() == 1;

        if (!isPaid) return -1;

        OptionalInt maxId = DataManager.getInstance()
            .getReturns()
            .stream()
            .mapToInt(CReturn::getReturnId)
            .max();

        int newId = !maxId.isPresent() ? 1 : (maxId.getAsInt() + 1);
        CReturn newCReturn = new CReturn(newId, sale.get());
        DataManager.getInstance().insertReturn(newCReturn);
            
        return newId;
    }


    @Override
    public boolean returnProduct(Integer returnId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {

    	if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }
    	
        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException();
        }
    	
        if (!isValidBarcode(productCode)) {
            throw new InvalidProductCodeException();
        }

        if (amount <= 0) {
            throw new InvalidQuantityException();
        }
    	 
    	 
        Optional<CReturn> Creturn = DataManager.getInstance()
            .getReturns()
            .stream()
            .filter(r -> r.getReturnId() == returnId)
            .findFirst();    
                
        Optional<it.polito.ezshop.model.ProductType> prod = DataManager.getInstance()
            .getProductTypes()
            .stream()
            .filter(p -> p.getBarCode().equals(productCode))
            .findFirst();
                
                
        if (!Creturn.isPresent() || !prod.isPresent()) return false;
        

        Sale sale = Creturn.get().getSaleTransaction();
        if (!sale.getProductsList().contains(prod.get())) return false;
        if (sale.getQuantityByProduct(prod.get()) - Creturn.get().getQuantityByProduct(prod.get()) < amount) return false;

    	Creturn.get().addProduct(prod.get(), amount);
    	
        return DataManager.getInstance().updateReturn(Creturn.get());
    }

    @Override
    public boolean returnProductRFID(Integer returnId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException();
        }
    	
        if (!isValidRFID(RFID)) {
            throw new InvalidRFIDException();
        }
        
        Optional<Product> prod = DataManager.getInstance()
            .getProducts()
            .stream()
            .filter(p -> p.getRFID().equals(RFID))
            .findFirst();

        if (!prod.isPresent() || prod.get().isAvailable()) return false;
        
        // boolean prodAlreadyReturned = DataManager.getInstance()
        //     .getReturns()
        //     .stream()
        //     .filter(ret -> ret.isCommitted())
        //     .anyMatch(ret -> ret.getProducRFIDs().contains(prod.get()));

        // if (prodAlreadyReturned) return false;

        Optional<CReturn> ret = DataManager.getInstance()
            .getReturns()
            .stream()
            .filter(r -> r.getReturnId() == returnId)
            .findFirst();

        if (!ret.isPresent() || ret.get().isCommitted() || ret.get().getProducRFIDs().contains(prod.get())) {
            return false;
        } else if (!ret.get().getSaleTransaction().getProducRFIDs().contains(prod.get())) {
            return false;
        }

        ret.get().addProductRFID(prod.get());

        return DataManager.getInstance().updateReturn(ret.get());
    }


    // Sale1, latte, 10, LatteType.quantity = 0
    // Ret1, not committed, latte x9
    // Ret2, not committed, latte x5
    // ret1.end() -> Sale -> latte 1
    // ret2.end() -> Sale -> latte -4, LatteType.quantity = 14

    @Override
    public boolean endReturnTransaction(Integer returnId, boolean commit) throws InvalidTransactionIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException();
        }

        Optional<CReturn> Creturn = DataManager.getInstance()
                .getReturns()
                .stream()
                .filter(r -> r.getReturnId() == returnId)
                .findFirst();
    	 

        if (!Creturn.isPresent() || Creturn.get().isCommitted()) return false;

        if (commit) {

            boolean cannotBeCommitted = Creturn.get()
                .getProductsList()
                .stream()
                .anyMatch(prod -> Creturn.get().getSaleTransaction().getQuantityByProduct(prod) - Creturn.get().getSaleTransaction().getReturnedQuantityByProduct((it.polito.ezshop.model.ProductType)prod) < Creturn.get().getQuantityByProduct(prod));

            cannotBeCommitted |= Creturn.get()
                .getProducRFIDs()
                .stream()
                .anyMatch(p -> !Creturn.get().getSaleTransaction().isProductRFIDReturned(p));

            if (cannotBeCommitted) return false;
            
            Creturn.get()
                .getProductsList()
                .forEach(p -> {
                    it.polito.ezshop.model.ProductType rightP = (it.polito.ezshop.model.ProductType)p;
                    
                    rightP.addQuantityOffset(Creturn.get().getQuantityByProduct(rightP));
                    DataManager.getInstance().updateProductType(rightP);
                });
            
            // Sale1 ..
            // Ret1, not committed, latte1, latte2, latte3
            // Ret2, not committed, latte 2

            //Ret2.commit()
            //Sale2.sell(latte2)
            //Ret1.commit() -> latte1, latte3

            Creturn.get()
                .getProducRFIDs()
                .stream()
                //.filter(p -> !Creturn.get().getSaleTransaction().isProductRFIDReturned(p))
                .forEach(p -> {
                    p.setAvailable(true);
                    p.getRelativeProductType().addQuantityOffset(1);
                    DataManager.getInstance().updateProduct(p);
                });

        } else {
            return DataManager.getInstance().deleteReturn(Creturn.get());
        }

        Creturn.get().setAsCommitted();
        return DataManager.getInstance().updateReturn(Creturn.get());
    }

    @Override
    public boolean deleteReturnTransaction(Integer returnId) throws InvalidTransactionIdException, UnauthorizedException {
     
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException();
        }
        

        Optional<CReturn> Creturn = DataManager.getInstance()
                .getReturns()
                .stream()
                .filter(r -> r.getReturnId() == returnId)
 	            .findFirst();
        

        if (!Creturn.isPresent() || Creturn.get().isCommitted()) return false;
 
        return DataManager.getInstance().deleteReturn(Creturn.get());
    }
    
    @Override
    public double receiveCashPayment(Integer ticketNumber, double cash) throws InvalidTransactionIdException, InvalidPaymentException, UnauthorizedException {
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (ticketNumber == null || ticketNumber <= 0) {
            throw new InvalidTransactionIdException();
        }

        if (cash <= 0) {
            throw new InvalidPaymentException();
        }

        Optional<Sale> sale = DataManager.getInstance()
            .getSales()
            .stream()
            .filter(r -> r.getTicketNumber() == ticketNumber)
            .findFirst();

        if(!sale.isPresent() || !sale.get().isCommitted()) return -1;
        if(sale.get().getTotalValue() > cash) return -1;

        OptionalInt maxBalId = DataManager.getInstance()
            .getBalanceTransactions()
            .stream()
            .mapToInt(it.polito.ezshop.model.BalanceTransaction::getBalanceId)
            .max();
        
        int newBalId = !maxBalId.isPresent() ? 1 : (maxBalId.getAsInt() + 1);
        
        BalanceTransaction bt = new CreditTransaction(newBalId, sale.get());
        if(!DataManager.getInstance().insertBalanceTransaction(bt)) return -1;

        return cash - sale.get().getTotalValue();
    }

    @Override
    public boolean receiveCreditCardPayment(Integer ticketNumber, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (ticketNumber == null || ticketNumber <= 0) {
            throw new InvalidTransactionIdException();
        }

        if(creditCard == null || creditCard.isEmpty() ||  !CreditCardSystem.getInstance().isValidNumber(creditCard)){
            throw new InvalidCreditCardException();
        }

        Optional<Sale> sale = DataManager.getInstance()
                                .getSales()
                                .stream()
                                .filter(r -> r.getTicketNumber() == ticketNumber)
                                .findFirst();

        if(!sale.isPresent() || !sale.get().isCommitted()) return false;

        if(!CreditCardSystem.getInstance().isRegistered(creditCard)) return false;
        if(!CreditCardSystem.getInstance().hasEnoughBalance(creditCard, sale.get().getTotalValue())) return false;

        OptionalInt maxBalId = DataManager.getInstance()
                 .getBalanceTransactions()
                 .stream()
                 .mapToInt(it.polito.ezshop.model.BalanceTransaction::getBalanceId)
                 .max();
        int newBalId = !maxBalId.isPresent() ? 1 : (maxBalId.getAsInt() + 1);

        BalanceTransaction bt = new CreditTransaction(newBalId, sale.get());
         
        if(!DataManager.getInstance().insertBalanceTransaction(bt)) return false;
         
        CreditCardSystem.getInstance().updateBalance(creditCard, sale.get().getTotalValue());

        return true;
    }

    @Override
    public double returnCashPayment(Integer returnId) throws InvalidTransactionIdException, UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException();
        }
        
        Optional<CReturn> Creturn = DataManager.getInstance()
                .getReturns()
                .stream()
                .filter(r -> r.getReturnId() == returnId)
                .findFirst();
        

        if (!Creturn.isPresent() || !Creturn.get().isCommitted()) return -1;
        
        OptionalInt maxBalId = DataManager.getInstance()
                .getBalanceTransactions()
                .stream()
                .mapToInt(it.polito.ezshop.model.BalanceTransaction::getBalanceId)
                .max();

        int newBalId = !maxBalId.isPresent() ? 1 : (maxBalId.getAsInt() + 1);

        Creturn.get().setBalanceId(newBalId);
        DataManager.getInstance().updateReturn(Creturn.get());
    
        BalanceTransaction bt = new DebitTransaction(newBalId,Creturn.get());
        DataManager.getInstance().insertBalanceTransaction(bt);
        	  
    	return Creturn.get().getTotalValue();
    }

    @Override
    public double returnCreditCardPayment(Integer returnId, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
    	 
        if (!RightsManager.getInstance().canManageSaleTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException();
        }

        if (!CreditCardSystem.getInstance().isValidNumber(creditCard)) {
            throw new InvalidCreditCardException();
        }

        if (!CreditCardSystem.getInstance().isRegistered(creditCard)) return -1;
        
        Optional<CReturn> Creturn = DataManager.getInstance()
                .getReturns()
                .stream()
                .filter(r -> r.getReturnId() == returnId)
                .findFirst();
        

        if (!Creturn.isPresent() || !Creturn.get().isCommitted()) return -1;

    
        OptionalInt maxBalId = DataManager.getInstance()
                .getBalanceTransactions()
                .stream()
                .mapToInt(it.polito.ezshop.model.BalanceTransaction::getBalanceId)
                .max();
        int newBalId = !maxBalId.isPresent() ? 1 : (maxBalId.getAsInt() + 1);
        
        Creturn.get().setBalanceId(newBalId);
    
        BalanceTransaction bt = new DebitTransaction(newBalId,Creturn.get());
        
        if(!DataManager.getInstance().insertBalanceTransaction(bt)) return -1;
        
        if(!CreditCardSystem.getInstance().updateBalance(creditCard, -Creturn.get().getTotalValue())) return -1;
        
        return Creturn.get().getTotalValue();
    }

    @Override 
    public boolean recordBalanceUpdate(double toBeAdded) throws UnauthorizedException {
        
        if (!RightsManager.getInstance().canManageBalanceTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        double totalBalance = computeBalance();

        if (totalBalance + toBeAdded >= 0) {

            OptionalInt maxId = DataManager.getInstance()
                .getBalanceTransactions()
                .stream()
                .mapToInt(BalanceTransaction::getBalanceId)
                .max();

            int id = !maxId.isPresent() ? 1 : (maxId.getAsInt() + 1);
            BalanceTransaction bt = null;

            if (toBeAdded >= 0) {

                maxId = DataManager.getInstance()
                    .getDummyCredits()
                    .stream()
                    .mapToInt(DummyCredit::getId)
                    .max();
        
                int newId = !maxId.isPresent() ? 1 : (maxId.getAsInt() + 1);

                DummyCredit dc = new DummyCredit(newId, toBeAdded);
                CreditTransaction ct = new CreditTransaction(id, dc);

                bt = ct;
                DataManager.getInstance().insertDummyCredit(dc);
            } else {

                maxId = DataManager.getInstance()
                    .getDummyDebits()
                    .stream()
                    .mapToInt(DummyDebit::getId)
                    .max();
        
                int newId = !maxId.isPresent() ? 1 : (maxId.getAsInt() + 1);

                DummyDebit dd = new DummyDebit(newId, -toBeAdded);
                DebitTransaction dt = new DebitTransaction(id, dd);

                bt = dt;
                DataManager.getInstance().insertDummyDebit(dd);
            }
            
            return DataManager.getInstance().insertBalanceTransaction(bt);
        }

        return false;
    }

    @Override
    public List<BalanceOperation> getCreditsAndDebits(LocalDate from, LocalDate to) throws UnauthorizedException {

        if (!RightsManager.getInstance().canManageBalanceTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }
        
        List<BalanceOperation> returnList = new ArrayList<BalanceOperation>();
            
        for (int i=0; i < DataManager.getInstance().getBalanceTransactions().size(); i++){
            LocalDate date = DataManager.getInstance().getBalanceTransactions().get(i).getDate();
            if ((from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to))){
                returnList.add(DataManager.getInstance().getBalanceTransactions().get(i));
            }
        }

        return returnList;
    }

    @Override
    public double computeBalance() throws UnauthorizedException {

        if (!RightsManager.getInstance().canManageBalanceTransactions(LoginManager.getInstance().getLoggedUser())) {
            throw new UnauthorizedException();
        }

        double balance = 0;
        
        for(BalanceTransaction b : DataManager.getInstance().getBalanceTransactions()){
            balance += (b instanceof CreditTransaction) ? b.getValue() : -b.getValue();
        }

        return getRightDoublePrecision(balance);
    }

    public static boolean isValidBarcode(String barcode) {

        if (barcode == null || barcode.length() < 12 || barcode.length() > 14) return false;

        if (!barcode.chars().allMatch(ch -> ch >= '0' && ch <= '9')) return false;


        int sum = 0;
        int mul_by = (barcode.length() % 2) == 0 ? 3 : 1;
        for (int i = 0; i < barcode.length() - 1; i++) {
            sum += Integer.valueOf(barcode.substring(i, i+1)) * mul_by;
            mul_by ^= 0x2;
        }

        int compare_num = sum;
        while (compare_num % 10 != 0) {
            compare_num++;
        }

        return (int)(barcode.charAt(barcode.length() - 1) - '0') == (compare_num - sum);
    }

    public static boolean isValidRFID(String RFID) {

        if (RFID == null || RFID.isEmpty() || RFID.length() != 12) return false;

        if (!RFID.chars().allMatch(ch -> ch >= '0' && ch <= '9')) return false;

        return true;
    }

    public static double getRightDoublePrecision(double value) {
        return BigDecimal.valueOf(value)
            .setScale(3, RoundingMode.HALF_UP)
            .doubleValue();
    }

    public static double getRightDoublePrecision(String value) {
        return BigDecimal.valueOf(Double.parseDouble(value))
            .setScale(3, RoundingMode.HALF_UP)
            .doubleValue();
    }

}

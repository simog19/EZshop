# Design Document 


Authors: Baldazzi Alessandro, D'Anzi Francesco, Galota Simone, La Greca Salvatore Gabriele

Date: 29/04/2021

Version: 1.1


# Contents

- [High level design](#package-diagram)
- [Low level design](#class-diagram)
- [Verification traceability matrix](#verification-traceability-matrix)
- [Verification sequence diagrams](#verification-sequence-diagrams)

# Instructions

The design must satisfy the Official Requirements document, notably functional and non functional requirements

# High level design 

Patterns used:
- Facade
- Singleton

Architectural styles used:
- 3-tier architecture
- MV

```plantuml
package "GUI" as GUI #DDDDDD {

}

package "Application Logic" as Backend {

    package "it.polito.ezshop.data" as data {

    }

    package "it.polito.ezshop.exceptions" as excp {

    }

    package "it.polito.ezshop.model" as model {

    }

    data ..> excp
    data ..> model

}

GUI .up.> data
```

# Low level design


```plantuml

package "it.polito.ezshop.data" as data {

    interface "EZShopInterface" as ezinterface {
        +reset(): void
        +createUser(): Integer
        +deleteUser(): boolean
        +getAllUsers(): List<User>
        +getUser(): User
        +updateUserRights(): boolean
        +login(): User
        +logout(): boolean
        +createProductType(): Integer
        +updateProduct(): boolean
        +deleteProductType(): boolean
        +getAllProductTypes(): List<ProductType>
        +getProductTypeByBarCode(): ProductType
        +getProductTypesByDescription(): List<ProductType>
        +updateQuantity(): boolean
        +updatePosition(): boolean
        +issueOrder(): Integer
        +payOrderFor(): Integer
        +payOrder(): boolean
        +recordOrderArrival(): boolean
        +getAllOrders(): List<Order>
        +defineCustomer(): Integer
        +modifyCustomer(): boolean
        +deleteCustomer(): boolean
        +getCustomer(): Customer
        +getAllCustomers(): List<Customer>
        +createCard(): String
        +attachCardToCustomer(): boolean
        +modifyPointsOnCard(): boolean
        +startSaleTransaction(): Integer
        +addProductToSale(): boolean
        +deleteProductFromSale(): boolean
        +applyDiscountRateToProduct(): boolean
        +applyDiscountRateToSale(): boolean
        +computePointsForSale(): int
        +endSaleTransaction(): boolean
        +deleteSaleTransaction(): boolean
        +getSaleTransaction(): SaleTransaction
        +startReturnTransaction(): Integer
        +returnProduct(): boolean
        +endReturnTransaction(): boolean
        +deleteReturnTransaction(): boolean
        +receiveCashPayment(): double
        +receiveCreditCardPayment(): boolean
        +returnCashPayment(): double
        +returnCreditCardPayment(): double
        +recordBalanceUpdate(): boolean
        +getCreditsAndDebits(): List<BalanceOperation>
        +computeBalance(): double
    }

    class "EZShop" as ezshop {

    }

    class LoginManager << (S,#FF7700) Singleton >> {
        -loggedUser: User
        +tryLogin(): boolean
        +tryLogout(): boolean
        +isUserLogged(): boolean
        +getLoggedUser(): User
    }

    class RightsManager << (S,#FF7700) Singleton >> {
        +canManageUsers(): boolean
        +canManageProductCatalog(): boolean
        +canListAllProductsTypes(): boolean
        +canManageInventory(): boolean
        +canManageCustomers(): boolean
        +canManageSaleTransaction(): boolean
        +canManagePayments(): boolean
        +canManageAccounting(): boolean
    }

    class "DataManager" as DataManager << (S,#FF7700) Singleton >> {
        ..Getters..
        +getUsers(): List<User>
        +getProductTypes(): List<ProductType>
        +getProducts(): List<Product>
        +getPositions(): List<Position>
        +getOrders(): List<Order>
        +getCustomers(): List<Customer>
        +getLoyaltyCards(): List<LoyaltyCard>
        +getSales(): List<Sale>
        +getReturns(): List<CReturn>
        +getDummyCredit(): List<DummyCredit>
        +getDummyDebit(): List<DummyDebit>
        +getBalanceOperations(): List<BalanceOperation>
        ..Data Insert..
        +insertUser(): boolean
        +insertProductType(): boolean
        +insertProduct(): boolean
        +insertOrder(): boolean
        +insertCustomer(): boolean
        +insertLoyaltyCard(): boolean
        +insertSale(): boolean
        +insertReturn(): boolean
        +insertPosition(): boolean
        +insertDummyCredit(): boolean
        +insertDummyDebit(): boolean
        +insertBalanceOperation(): boolean
        ..Data Update..
        +updateUser(): boolean
        +updateProductType(): boolean
        +updateProduct(): boolean
        +updateOrder(): boolean
        +updateCustomer(): boolean
        +updateLoyaltyCard(): boolean
        +updateSale(): boolean
        +updateReturn(): boolean
        +updatePosition(): boolean
        +updateDummyCredit(): boolean
        +updateDummyDebit(): boolean
        +updateBalanceOperation(): boolean
        ..Data Delete..
        +deleteUser(): boolean
        +deleteProductType(): boolean
        +deleteProduct(): boolean
        +deleteOrder(): boolean
        +deleteCustomer(): boolean
        +deteleteLoyaltyCard(): boolean
        +deleteSale(): boolean
        +deleteReturn(): boolean
        +deletePosition(): boolean
        +deleteDummyCredit(): boolean
        +deleteDummyDebit(): boolean
        +deleteBalanceOperation(): boolean
    }

    class CreditCardSystem << (S,#FF7700) Singleton >> {
        -creditsCardBalance: Map<String, Double>
        +isValidNumber(): boolean
        +isRegistered(): boolean
        +hasEnoughBalance(): boolean
        +updateBalance(): boolean
    }

    note right of RightsManager
        <b>Manages users' rights</b>
        (checks if a generic User
        (passed as argument) can
        do a certain action)
    end note

    note top of DataManager
        <b>Manages all App's data</b>
        (interface towards backend data
        storage, complete transparent to
        the entire system)
    end note

    'note left of LoginManager::isUserLogged
    '    This method accepts both
    '    0 or 1 argument.
    '    - If 0, checks if an user is logged in
    '    (i.e. getLoggedUser() != null)
    '    - If 1, it's an <<User>> object and checks
    '    if this user is actually logged
    '    (i.e. getLoggedUser() == arg_user)
    'end note

    ezshop -up-|> ezinterface : <<implements>>
    ezshop ..> LoginManager
    ezshop ..> RightsManager
    ezshop .left.> DataManager
    ezshop .right.> CreditCardSystem

    RightsManager .left.> LoginManager
    LoginManager .up.> DataManager
    

}

```


```plantuml

package "it.polito.ezshop.model" as model {
    class User <<persistent>> {
        -ID: Integer
        -username: String
        -password: String
        -role: String
        {method} +setRole(): void
        +getRole(): string
    }

    class ProductType <<persistent>> {
        -productId: Integer
        -barcode: String
        -description: String
        -selfPrice: Double
        -quantity: Integer
        -discountRate: Double
        -notes: String
        -position: Position
        +addQuantityOffset(): boolean
        +getAssignedPosition(): Position
        +assignToPosition(): void
    }

    class Product <<persistent>> {
        -rfid: String
        -RelativeProductType: ProductType
        -avaiable: boolean
        +setAvailable(): void
        +isAvailable(): boolean
        +getRelatedProductType(): boolean
        +getRFID(): String
    }

    class Position <<persistent>> {
        -aisleID: Integer
        -rackID: String
        -levelID: Integer
        -product: ProductType
        ~assignToProduct(): void
        +getAssignedProduct(): ProductType
        +toString(): String
    }

    abstract ProductList <<abstract>> {
        ~products: Map<ProductType, Integer>
        +getProductsList(): List<ProductType>
        +getQuantityByProduct(): Integer
        +addProduct(): void
    }

    class Sale <<persistent>> {
        -saleId: Integer
        -date: Date
        -discountRate: Double
        -loyaltyCard: LoyaltyCard
        -committed: boolean
        -productsDiscountRate: Map<ProductType, double>
        -returnTransaction: List<ReturnTransaction>
        -productRFIDs: List<Product>
        ~addReturnTransaction(): void
        +applyDiscountRateToSale(): void
        +applyDiscountRateToProductGroup(): void
        +attachLoyaltyCard(): void
        +getAttachedLoyaltyCard(): LoyaltyCard
        +setAsCommitted(): void
        +getSaleId(): Integer
        +isCommitted(): boolean
        +addProductRFID(): boolean
        +deleteProductRFID(): boolean
        +getProductRFIDs(): List<Product>
    }

    class CReturn <<persistent>> {
        -returnId: Intger
        -saleTransaction: Sale
        -committed: boolean
        -productRFIDs: List<Product>
        +addProduct(): void <<override>>
        +setAsCommitted(): void
        +getReturnid(): Double
        +isCommitted(): boolean
        +addProductRFID(): boolean
        +deleteProductRFID(): boolean
        +getProductRFIDs(): List<Product>
    }

    enum EOrderStatus {
        +ISSUED
        +PAYED
        +COMPLETED
    }

    class Order <<persistent>> {
        -orderId: Integer
        -supplier: String
        -pricePerUnit: Double
        -quantity: Integer
        -product: ProductType
        -status: EOrderStatus 
        +getOrderID(): Integer
        +getQuantity(): Integer
        +getRelatedProduct(): ProductType
        +getStatus(): EOrderStatus
        +setAsPayed(): void
        +setAsCompleted(): void
    }

    abstract BalanceTransaction <<abstract>> <<persistent>> {
        -balanceId: Integer
        -description: String
        -value: Double
        +getTransactionType(): ETransactionType
        +getTotalValue(): Double
    }

    class Customer <<persistent>> {
        -customerID: Integer
        -name: String
        -loyaltyCard: LoyaltyCard
        +attachLoyaltyCard(): void
        +setName(): void
    }

    class LoyaltyCard <<persistent>> {
        -ID: String
        -points: Integer
        -customer: Customer
        +addCustomer(): void
        +addPoints(): void
        +getPoints(): void
    }

    interface ICredit <<interface>> {
        +getTotalValue(): Double
    }

    interface IDebit <<interface>> {
        +getTotalValue(): Double
    }

    class CreditTransaction {
        -relatedCreditOperation: ICredit
        +getRelated(): ICredit
    }

    class DebitTransaction {
        -relatedDebitOperation: IDebit
        +getRelated(): IDebit
    }

    class DummyCredit <<persistent>> {
        -value: Double
    }

    class DummyDebit <<persistent>> {
        -value: Double
    }

    ProductType <-right-> Position
    Product -right-> ProductType
    Sale <-right- CReturn

    Sale --> Product
    CReturn --> Product

    LoyaltyCard <--> Customer
    'Sale <-down-> LoyaltyCard

    'BalanceTransaction <|-- SaleTransaction
    'BalanceTransaction <|-- ReturnTransaction
    'BalanceTransaction <|-- OrderTransaction
    'BalanceTransaction <|-- DummyTransaction

    'OrderTransaction --> Order
    Order --> ProductType
    'SaleTransaction --> Sale

    'CReturn <-up- ReturnTransaction 

    Order -right-> EOrderStatus

    ProductList <|-up- Sale
    ProductList <|-up- CReturn
    ProductList --> ProductType

    Sale -up-|> ICredit
    Order -up-|> IDebit
    CReturn -up-|> IDebit
    DummyCredit -left-|> ICredit
    DummyDebit -left-|> IDebit

    BalanceTransaction <|-- CreditTransaction
    BalanceTransaction <|-- DebitTransaction

    CreditTransaction --> ICredit
    DebitTransaction --> IDebit

}

```

```plantuml
package "it.polito.ezshop.exceptions" {
    class InvalidCreditCardException {
    }

    class InvalidCustomerCardException {
    }

    class InvalidCustomerIdException {
    }

    class InvalidCustomerNameException {
    }

    class InvalidDiscountRateException {
    }

    class InvalidLocationException {
    }

    class InvalidOrderIdException {
    }

    class InvalidPasswordException {
    }

    class InvalidPaymentException {
    }

    class InvalidPricePerUnitException {
    }

    class InvalidProductCodeException {
    }

    class InvalidProductDescriptionException {
    }

    class InvalidProductIdException {
    }

    class InvalidQuantityException {
    }

    class InvalidRoleException {
    }

    class InvalidTransactionIdException {
    }

    class InvalidUserIdException {
    }

    class InvalidUsernameException {
    }

    class UnauthorizedException {
    }


}

```


# Verification traceability matrix
|     | EZShop | DataManager | LoginManager | RightsManager | CreditCardSystem | User | CreditTransaction | DebitTransaction | DummyDebit | DummyCredit | Sale | CReturn | Order | LoyaltyCard | ProductType | Customer | Position |
|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| FR1 | X      | X           | X            | X             |                   | X    |                   |                  |            |             |      |         |       |             |             |          |          |
| FR3 | X      | X           | X            | X             |                   | X    |                   |                  |            |             |      |         |       |             | X           |          |          |
| FR4 | X      | X           | X            | X             |                   | X    |                   | X                |            |             |      |         | X     |             | X           |          | X        |
| FR5 | X      | X           | X            | X             |                   | X    |                   |                  |            |             |      |         |       | X           |             | X        |          |
| FR6 | X      | X           | X            | X             |                   | X    | X                 | X                |            |             | X    | X       |       |             |             |          |          |
| FR7 | X      | X           | X            | X             | X                 | X    |                   |                  |            |             |      |         |       |             |             |          |          |
| FR8 | X      | X           | X            | X             |                   | X    | X                 | X                | X          | X           |      |         |       |             |             |          |          |














# Verification sequence diagrams 

## Scenario 1-1

```plantuml
participant GUI

GUI -> EZShop: createProductType()
activate EZShop

EZShop -> RightManager: canManageProductCatalogue()
activate RightManager

RightManager -> LoginManager: isUserLogged()
activate LoginManager
LoginManager -> RightManager: user is logged
deactivate LoginManager

RightManager -> EZShop: user has permissions
deactivate RightManager

EZShop -> DataManager: insertProductType()
activate DataManager
DataManager -> EZShop: product type created and saved in the DB
deactivate DataManager

EZShop -> GUI: Done
deactivate EZShop
```

## Scenario 2-1

```plantuml
participant GUI

GUI -> EZShop: createUser()
activate EZShop

EZShop -> RightManager: canManageUsers()
activate RightManager

RightManager -> LoginManager: isUserLogged()
activate LoginManager
LoginManager -> RightManager: user is logged
deactivate LoginManager

RightManager -> EZShop: user has permissions
deactivate RightManager

EZShop -> DataManager: insertUser()
activate DataManager
DataManager -> EZShop: user created and saved in the DB
deactivate DataManager

EZShop -> GUI: Done
deactivate EZShop
```
## Scenario 6-1
```plantuml
participant GUI

GUI -> EZShop: 1: startSaleTransaction()
activate EZShop

EZShop -> RightManager: 2: canManageUsers()
activate RightManager

RightManager -> LoginManager: 3: isUserLogged()
activate LoginManager
LoginManager -> RightManager: user is logged
deactivate LoginManager

RightManager -> EZShop: user has permissions
deactivate RightManager

EZShop -> DataManager: 4: insertSale()
activate DataManager

DataManager -> EZShop: Sale created and saved in the DB
deactivate DataManager

EZShop -> GUI: Done
deactivate EZShop

GUI->EZShop: 5: addProductToSale()
activate EZShop

EZShop -> DataManager: 6: getSales()
activate DataManager

DataManager -> EZShop: got sale interested
deactivate DataManager

EZShop -> DataManager: 7: getProductTypes()
activate DataManager

DataManager -> EZShop: got Product interested
deactivate DataManager


EZShop -> Sale: 8: addProduct()
activate Sale

Sale -> EZShop: product added to sale
deactivate Sale


EZShop -> ProductType: 9: addQuantityOffset()
activate ProductType

ProductType -> EZShop: product quantity updated
deactivate ProductType

EZShop -> DataManager: 10: UpdateProductType()
activate DataManager

DataManager -> EZShop: quantity updated
deactivate DataManager

EZShop -> DataManager: 11: UpdateSale()
activate DataManager

DataManager -> EZShop: sale updated
deactivate DataManager


EZShop -> GUI: Done
deactivate EZShop


GUI -> EZShop: 12: endSaleTransaction()
activate EZShop

EZShop -> DataManager: 13: getSales()
activate DataManager

DataManager -> EZShop: got sale interested
deactivate DataManager

EZShop -> Sale: 14: SetAsCommitted()
activate Sale

Sale -> EZShop: sale committed
deactivate Sale

EZShop -> DataManager: 11: UpdateSale()
activate DataManager

DataManager -> EZShop: sale updated
deactivate DataManager

EZShop -> GUI: Done
deactivate EZShop
```

## Scenario 7-1

```plantuml
participant GUI

GUI -> EZShop: receiveCreditCardPayment()
activate EZShop

EZShop -> RightManager: canManagePayments()
activate RightManager

RightManager -> LoginManager: isUserLogged()
activate LoginManager
LoginManager -> RightManager: user is logged
deactivate LoginManager

RightManager -> EZShop: user has permissions
deactivate RightManager

EZShop -> CreditCardSystem: isValidNumber()
activate CreditCardSystem
CreditCardSystem -> EZShop: the number is valid
deactivate CreditCardSystem

EZShop -> DataManager: getSales()
activate DataManager

DataManager -> EZShop: return list of Sales, extraction of the required one
deactivate DataManager

EZShop -> Sale: getTotal()
activate Sale
Sale -> EZShop: the total is computed and returned
deactivate Sale

EZShop -> CreditCardSystem: hasEnoughBalance()
activate CreditCardSystem
CreditCardSystem -> EZShop: the balance is enough
deactivate CreditCardSystem

EZShop -> CreditCardSystem: updateBalance()
activate CreditCardSystem
CreditCardSystem -> EZShop: the balance is updated
deactivate CreditCardSystem

EZShop -> EZShop: creates balance transaction (as CreditTransaction)

EZShop -> DataManager: insertBalanceTransaction()
activate DataManager

DataManager -> EZShop: transaction recorded
deactivate DataManager

EZShop -> GUI: Done
deactivate EZShop
```

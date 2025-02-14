@runAll
Feature: BT Paypal Auth

  @positive @smokeTest
  Scenario Outline: Verifying of BT Paypal auth data response
    Given "<userType>" user received token for site "<aeSite>"
    And user received their orderId
    And bag successfully cleared
    And bag successfully populated with inventory check value "false" and items with parameters from the table
      | ddfType  | quantity |
      | IN_STOCK | 1        |
    And save shipping address "<address>" with address validation flags verified: "false" and verifiedIgnored: "true"
    And the billing address "<address>" is saved
    And "STD" shipping method successfully saved
    And response from view cart with the referer header set to checkout and inventory check "true" successfully saved
    And user views the bag
    And user adds PayPal payment method
    When user authorizes the payment with the following data
      | orderNumber  | cartId      | currencyCode | channelType | profileId   | address1        | city   | country | firstName | lastName | postalCode | state | shippingMethod | email    | phoneNo      | amount | paypalToken      |
      | randomNumber | fromContext | USD          | WEB         | fromContext | 3122 w 38th Ave | Denver | US      | John      | Doe      | 80211      | CO    | STD            | me@me.me | +16364741174 | 100    | fake-valid-nonce |
    Then expected response code: 200

    Examples:
      | userType              | aeSite | address    |
      | randomly created user | AEO_US | US_address |
      | guest                 | AEO_US | US_address |
      | randomly created user | AEO_CA | CA_address |
      | guest                 | AEO_CA | CA_address |

  @negative
  Scenario Outline: Verifying of BT Paypal auth data response
    Given "<userType>" user received token for site "<aeSite>"
    And user received their orderId
    And bag successfully cleared
    And bag successfully populated with inventory check value "false" and items with parameters from the table
      | ddfType  | quantity |
      | IN_STOCK | 1        |
    And save shipping address "<address>" with address validation flags verified: "false" and verifiedIgnored: "true"
    And the billing address "<address>" is saved
    And "STD" shipping method successfully saved
    And response from view cart with the referer header set to checkout and inventory check "true" successfully saved
    And user views the bag
    And user adds PayPal payment method
    When user authorizes the payment with the following data
      | orderNumber  | cartId      | currencyCode | channelType | profileId   | address1        | city   | country | firstName | lastName | postalCode | state | shippingMethod | email    | phoneNo      | amount | paypalToken   |
      | randomNumber | fromContext | USD          | WEB         | fromContext | 3122 w 38th Ave | Denver | US      | John      | Doe      | 80211      | CO    | STD            | me@me.me | +16364741174 | 100    | <paypalToken> |
    Then expected response code: 422

    Examples:
      | userType              | aeSite | address    | paypalToken                         |
      | randomly created user | AEO_US | US_address | fake-paypal-one-time-nonce          |
      | guest                 | AEO_US | US_address | fake-paypal-billing-agreement-nonce |
      | randomly created user | AEO_CA | CA_address | payment_method_nonce                |
      | guest                 | AEO_CA | CA_address | fake-invalid-nonce                  |
      
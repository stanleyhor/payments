@runAll @guest
Feature: AfterPay Checkout on Cloud tests

  @AfterPayCoC @positive
  Scenario: Verify that afterPay data is stored in the DB after a successful authorization
    Given "guest" user received token for site "AEO_US"
    And user received their orderId
    And bag successfully cleared
    And bag successfully populated with inventory check value "false" and items with parameters from the table
      | ddfType | quantity |
      | REGULAR | 9        |
    And save shipping address "US_address" with address validation flags verified: "false" and verifiedIgnored: "true"
    And the billing address "US_address" is saved
    And "STD" shipping method successfully saved
    And response from view cart with the referer header set to checkout and inventory check "true" successfully saved
    And user views the bag
    And user adds AfterPay payment method
    When user authorizes the payment with the following data
      | orderNumber  | cartId      | currencyCode | channelType | profileId   | address1        | city   | country | firstName | lastName | postalCode | state | shippingMethod | email    | phoneNo       | afterpayEligible | cashAppPayEligible | returnUrl                             |
      | randomNumber | fromContext | USD          | WEB         | fromContext | 3122 w 38th Ave | Denver | US      | John      | Doe      | 80211      | CO    | STD            | me@me.me | +16364741174  | true             | true               | https://sit.aezone.com/us/en/checkout |
    And expected response code: 200
    Then the authorization response contains the following data
      | orderNumber | token       | expires     | redirectCheckoutUrl | httpStatusCode | resultCode        |
      | fromContext | fromContext | fromContext | fromContext         | 0              | REDIRECT_AFTERPAY |
    When the user verifies the payment info saved in the DB
    Then the correct data is returned after authorizing the order
      | paymentVariation |
      | afterPay         |
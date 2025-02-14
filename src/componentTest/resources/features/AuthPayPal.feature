@runAll @paypal
Feature: PayPal Auth
  
  @positive @smokeTest @envStability
  Scenario: Verifying of PayPal auth data response
    Given "guest" user received token for site "AEO_INTL"
    And read from file "PAYPAL"
    When user sets prop "billingAddress.email" = "aeprodcc@ae.com"
    And User tries to post request
    Then expected response code: 200
    And get data from scenarioContext
    And compare schema of saved and received response for "PAYPAL"
    And property "additionalData.paymentMethod" is "paypal"
    And property "amount.value" is "5000"
    And property "amount.currency" is "USD"

  @negative
  Scenario: Verifying of PayPal auth data response for incorrect shopperInteraction
    Given "guest" user received token for site "AEO_INTL"
    And read from file "PAYPAL"
    When user sets prop "shopperInteraction" = ""
    And User tries to post request
    Then expected response code: 400

  @negative
  Scenario: Verifying of PayPal auth data response for incorrect idempotencyKey
    Given "guest" user received token for site "AEO_INTL"
    And read from file "PAYPAL"
    When user sets prop "idempotencyKey" = " "
    And User tries to post request
    Then expected response code: 500

  @negative
  Scenario: Verifying of PayPal auth data response for different idempotencyKey
    Given "guest" user received token for site "AEO_MX"
    And read from file "PAYPAL"
    And user sets random idempotencyKey
    And user sets random webstoreId
    And User tries to post request
    Then expected response code: 200
    And get data from scenarioContext
    And property "resultCode" is "DECLINED"
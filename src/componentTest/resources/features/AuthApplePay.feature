@runAll @applepay
Feature: ApplePay Auth

  @positive @smokeTest
  Scenario Outline: Verifying of ApplePay auth data response
    Given "guest" user received token for site "<aeSite>"
    And read from file "<paymentMethodFileName>"
    When user sets prop "billingAddress.email" = "aeprodcc@ae.com"
    And user sets random idempotencyKey
    And User tries to post request
    Then expected response code: 200
    And get data from scenarioContext
    And compare schema of saved and received response for "<paymentMethodFileName>"
    And property "additionalData.paymentMethod" is "applePay"
    And property "additionalData.creditCardType" is "<creditCardType>"
    And property "amount.value" is "<amount>"
    And property "amount.currency" is "<currency>"
    And property "resultCode" is "<resultCode>"

    Examples:
      | aeSite | paymentMethodFileName | creditCardType   | amount | currency | resultCode |
      | AEO_US | APPLEPAY              | mc_applepay      | 3507   | USD      | AUTHORIZED |
      | AEO_CA | APPLEPAY_INTERAC      | interac_applepay | 19865  | CAD      | SETTLED    |

  @negative
  Scenario Outline: Verifying of ApplePay auth data response for incorrect shopperInteraction
    Given "guest" user received token for site "<aeSite>"
    And read from file "<paymentMethodFileName>"
    When user sets prop "shopperInteraction" = ""
    And User tries to post request
    Then expected response code: 400

    Examples:
      | aeSite | paymentMethodFileName |
      | AEO_US | APPLEPAY              |
      | AEO_CA | APPLEPAY_INTERAC      |

  @negative
  Scenario Outline: Verifying of ApplePay auth data response for incorrect idempotencyKey
    Given "guest" user received token for site "<aeSite>"
    And read from file "<paymentMethodFileName>"
    When user sets prop "idempotencyKey" = " "
    And User tries to post request
    Then expected response code: 500

    Examples:
      | aeSite | paymentMethodFileName |
      | AEO_US | APPLEPAY              |
      | AEO_CA | APPLEPAY_INTERAC      |

  @negative @testing
  Scenario Outline: Verifying of ApplePay auth data response for different idempotencyKey
    Given "guest" user received token for site "<aeSite>"
    And read from file "<paymentMethodFileName>"
    And user sets random idempotencyKey
    And user sets random webstoreId
    And User tries to post request
    Then expected response code: 200
    And get data from scenarioContext
    And property "resultCode" is "DECLINED"

    Examples:
      | aeSite | paymentMethodFileName |
      | AEO_US | APPLEPAY              |
      | AEO_CA | APPLEPAY_INTERAC      |
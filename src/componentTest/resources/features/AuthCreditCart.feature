@runAll @creditCard
Feature: Payment Auth

  @positive @smokeTest
  Scenario Outline: Verifying of auth data response for regular cards
    Given "guest" user received token for site "<aeSite>"
    And read from file "CREDIT_CARD"
    And set credit card type "<creditCardType>"
    And user sets random idempotencyKey
    When user sets prop "amount.currency" = "<currency>"
    And user sets prop "amount.value" = "<amount>"
    And User tries to post request
    Then expected response code: 200
    And get data from scenarioContext
    And compare schema of saved and received response for "CREDIT_CARD"
    And property "amount.value" is "<amount>"
    And property "amount.currency" is "<currency>"
    And property "additionalData.creditCardType" is "<returnedCreditCardType>"

    Examples:
      | aeSite   | amount | currency | creditCardType   | returnedCreditCardType |
      | AEO_US   | 315    | USD      | MASTERCARD       | mc                     |
      | AEO_US   | 315    | USD      | DISCOVER         | discover               |
      | AEO_US   | 315    | USD      | DINERS           | diners                 |
      | AEO_US   | 315    | USD      | JCB              | jcb                    |
      | AEO_US   | 315    | USD      | AMERICAN_EXPRESS | amex                   |
      | AEO_MX   | 1312   | MXN      | VISA             | visa                   |
      | AEO_MX   | 1312   | MXN      | AMERICAN_EXPRESS | amex                   |
      | AEO_INTL | 1015   | USD      | VISA             | visa                   |
      | AEO_INTL | 1015   | USD      | DISCOVER         | discover               |
      | AEO_INTL | 1015   | USD      | JCB              | jcb                    |
      | AEO_INTL | 1015   | USD      | AMERICAN_EXPRESS | amex                   |
      | AEO_CA   | 1395   | CAD      | VISA             | visa                   |
      | AEO_CA   | 1395   | CAD      | MASTERCARD       | mc                     |
      | AEO_CA   | 1395   | CAD      | DINERS           | diners                 |
      | AEO_CA   | 1395   | CAD      | JCB              | jcb                    |
      | AEO_CA   | 1395   | CAD      | AMERICAN_EXPRESS | amex                   |

    @envStability @smokeTest @positive
    Examples:
      | aeSite   | amount | currency | creditCardType | returnedCreditCardType |
      | AEO_US   | 315    | USD      | VISA           | visa                   |
      | AEO_MX   | 1312   | MXN      | MASTERCARD     | mc                     |
      | AEO_INTL | 1015   | USD      | MASTERCARD     | mc                     |
      | AEO_CA   | 1395   | CAD      | DISCOVER       | discover               |


  @3ds
    @positive
  Scenario Outline: Verifying of auth data response for 3ds cards
    Given "guest" user received token for site "<aeSite>"
    And read from file "CREDIT_CARD_3DS"
    And set credit card type "<creditCardType>"
    And user sets random idempotencyKey
    When user sets prop "amount.currency" = "<currency>"
    And user sets prop "amount.value" = "<amount>"
    When user sets prop "executeThreeD" = "true"
    And User tries to post request
    Then expected response code: 200
    And get data from scenarioContext
    And compare schema of saved and received response for "CREDIT_CARD_3DS"
    And property "resultCode" is "REDIRECT_SHOPPER"
    And property "action.paymentData" is not empty
    And property "action.url" is not empty
    And property "action.type" is not empty
    And property "action.data.MD" is not empty
    And property "action.data.PaReq" is not empty
    And property "action.data.TermUrl" is not empty

    Examples:
      | aeSite | amount | currency | creditCardType       |
      | AEO_MX | 1312   | MXN      | VISA_3DS             |
      | AEO_MX | 1312   | MXN      | MASTERCARD_3DS       |
      | AEO_MX | 1312   | MXN      | AMERICAN_EXPRESS_3DS |

  @negative
  Scenario Outline: Verifying of auth data response for incorrect idempotencyKey
    Given "guest" user received token for site "<aeSite>"
    And read from file "CREDIT_CARD"
    When user sets prop "idempotencyKey" = " "
    When user sets prop "amount.currency" = "<currency>"
    When user sets prop "amount.value" = "<amount>"
    And User tries to post request
    Then expected response code: 500

    Examples:
      | aeSite   | amount | currency |
      | AEO_INTL | 315    | USD      |
      | AEO_MX   | 1312   | MXN      |
      | AEO_US   | 1015   | USD      |
      | AEO_CA   | 1395   | CAD      |
    
  @positive
  Scenario Outline: Verifying of auth data response for synchrony cards
    Given "guest" user received token for site "<aeSite>"
    And read from file "<cardType>"
    And set credit card type "<creditCardType>"
    And user sets random idempotencyKey
    When user sets prop "amount.currency" = "<currency>"
    And user sets prop "amount.value" = "<amount>"
    And User tries to post request
    Then expected response code: 200
    And get data from scenarioContext
    And compare schema of saved and received response for "<cardType>"
    And property "amount.value" is "<amount>"
    And property "amount.currency" is "<currency>"
    And property "additionalData.creditCardType" is "<returnedCreditCardType>"

    Examples:
      | aeSite   | amount | currency | creditCardType   | returnedCreditCardType | cardType              |
      | AEO_US   | 315    | USD      | AECREDITCARD     | synchrony_plcc         | SYNCHRONY_CREDIT_CARD |
      | AEO_US   | 425		|	USD			 | AEVISA					  | synchrony_cbcc				 | CREDIT_CARD					 |
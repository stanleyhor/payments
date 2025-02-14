@runAll
Feature: Auth Receiving Data
  @negative
  Scenario Outline: Receive auth data
    Given "guest" user received token for site "<aeSite>"
    And User tries to get response
    Then expected response code: 500

    Examples:
      |aeSite  |
      |AEO_INTL|
      |AEO_MX  |
      |AEO_US  |
      |AEO_CA  |

  @negative
  Scenario Outline: Receive auth data without paymentGroupId
    Given "guest" user received token for site "<aeSite>"
    And User tries to get request without paymentGroupId
    Then expected response code: 500

    Examples:
      |aeSite  |
      |AEO_INTL|
      |AEO_MX  |
      |AEO_US  |
      |AEO_CA  |
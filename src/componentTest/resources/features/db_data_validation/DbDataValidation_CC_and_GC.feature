@runAll
Feature: DB Data validation for CreditCard and GiftCard payments

  @dbValidation @creditCard
  Scenario Outline: Validate the data in the DB for the CreditCard payment
    Given "guest" user received token for site "<site>"
    And response from view cart successfully saved
    And bag successfully cleared
    And bag successfully populated with inventory check value "false" and items with parameters from the table
      | ddfType  | quantity |
      | IN_STOCK | 1        |
    When save shipping address "<address>" with address validation flags verified: "false" and verifiedIgnored: "true"
    And the billing address "<address>" is saved
    And "STD" shipping method successfully saved
    And response from view cart with the referer header set to checkout and inventory check "true" successfully saved
    When user views the bag
    When user views the atg cart
    When user adds a "MASTERCARD" credit card type with the default billing address
    When the order is placed with default parameters
    Then expected response code: 200
    When the user verifies the payment info saved in the DB
    Then the correct data is returned after placing the order
      | paymentVariation |
      | creditCard       |

    Examples:
      | site     | address      |
      | AEO_US   | US_address   |
      | AEO_CA   | CA_address   |
      | AEO_INTL | INTL_address |
      | AEO_MX   | MX_address   |


  @dbValidation @giftCard
  Scenario Outline: Validate the data in the DB for the GiftCard payments
    Given "guest" user received token for site "<site>"
    And response from view cart successfully saved
    And bag successfully cleared
    And bag successfully populated with inventory check value "false" and items with parameters from the table
      | ddfType  | quantity |
      | IN_STOCK | 1        |
    When save shipping address "<address>" with address validation flags verified: "false" and verifiedIgnored: "true"
    And the billing address "<address>" is saved
    And "STD" shipping method successfully saved
    And response from view cart with the referer header set to checkout and inventory check "true" successfully saved
    When user views the bag
    When user views the atg cart
    And user adds 3 gift cards with correct pin as payments
    When the order is placed with default parameters
    Then expected response code: 200
    When the user verifies the payment info saved in the DB
    Then the correct data is returned after placing the order
      | paymentVariation |
      | giftCard         |
      | giftCard         |
      | giftCard         |

    Examples:
      | site     | address      |
      | AEO_US   | US_address   |
      | AEO_CA   | CA_address   |
      | AEO_INTL | INTL_address |
      | AEO_MX   | MX_address   |


  @dbValidation @creditCard @giftCard
  Scenario Outline: Validate the data in the DB for the CreditCard and GiftCard payments
    Given "guest" user received token for site "<site>"
    And response from view cart successfully saved
    And bag successfully cleared
    And bag successfully populated with inventory check value "false" and items with parameters from the table
      | ddfType  | quantity |
      | IN_STOCK | 9        |
    When save shipping address "<address>" with address validation flags verified: "false" and verifiedIgnored: "true"
    And the billing address "US_address" is saved
    And "STD" shipping method successfully saved
    And response from view cart with the referer header set to checkout and inventory check "true" successfully saved
    When user views the bag
    When user views the atg cart
    When user adds a "MASTERCARD" credit card type with the default billing address
    And user adds 1 gift cards with correct pin as payments
    When the order is placed with default parameters
    Then expected response code: 200
    When the user verifies the payment info saved in the DB
    Then the correct data is returned after placing the order
      | paymentVariation |
      | creditCard       |
      | giftCard         |
      | giftCard         |
      | giftCard         |

    Examples:
      | site     | address      |
      | AEO_US   | US_address   |
      | AEO_CA   | CA_address   |
      | AEO_INTL | INTL_address |
      | AEO_MX   | MX_address   |
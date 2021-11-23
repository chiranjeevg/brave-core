/* This Source Code Form is subject to the terms of the Mozilla Public
 * License. v. 2.0. If a copy of the MPL was not distributed with this file.
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

import styled from 'styled-components'

export const TourPromoWrapper = styled('div')<{}>`
  margin-top: 30px;
`

export const PageWalletWrapper = styled.div`
  width: 373px;
`

export const PaymentStatus = styled.div`
  color: var(--brave-palette-neutral900);
  font-size: 14px;
  line-height: 24px;

  a {
    color: var(--brave-palette-blurple500);
    font-weight: 600;
    text-decoration: none;
  }

  .rewards-payment-pending {
    margin-bottom: 8px;
    background: #E8F4FF;
    border-radius: 4px;
    padding: 6px 21px;
    display: flex;
    align-items: center;
    font-weight: 600;
    gap: 6px;

    .icon {
      color: var(--brave-palette-blue500);
      height: 16px;
      width: auto;
      vertical-align: middle;
    }
  }

  .rewards-payment-amount {
    .plus {
      margin-right: 2px;
    }
  }

  .rewards-payment-completed {
    margin-bottom: 8px;
    background: #E7FDEA;
    border-radius: 4px;
    padding: 6px 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 16px;

    .icon {
      height: 43px;
      width: auto;
      vertical-align: middle;
    }
  }

  .rewards-payment-not-arrived {
    font-size: 12px;
    line-height: 22px;
    color: var(--brave-palette-neutral700);
  }
`

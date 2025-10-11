/**
 * [Phillip Watson] ("COMPANY") CONFIDENTIAL Unpublished Copyright © 2019-2021 Phillip Watson,
 * All Rights Reserved.
 * <p>
 * NOTICE: All information contained herein is, and remains the property of COMPANY. The
 * intellectual and technical concepts contained herein are proprietary to COMPANY and may be
 * covered by U.K. and Foreign Patents, patents in process, and are protected by trade secret or
 * copyright law. Dissemination of this information or reproduction of this material is strictly
 * forbidden unless prior written permission is obtained from COMPANY. Access to the source code
 * contained herein is hereby forbidden to anyone except current COMPANY employees, managers or
 * contractors who have executed Confidentiality and Non-disclosure agreements explicitly covering
 * such access.
 * <p>
 * The copyright notice above does not evidence any actual or intended publication or disclosure of
 * this source code, which includes information that is confidential and/or proprietary, and is a
 * trade secret, of COMPANY. ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR
 * PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF
 * COMPANY IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES.
 * THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY
 * ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL
 * ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.hillayes.accumulator;

import java.time.Instant;
import java.util.Optional;

/**
 * Represents units of time at which data is accumulated and viewed.
 * Each unit must be an exact division of its higher order unit.
 */
public interface Resolution {
    /**
     * Returns the name that identifies this Resolution instance.
     */
    String name();

    /**
     * Returns the Resolution that is the immediate lower Resolution of this one.
     * If this is the lowest Resolution, the result will be an empty Optional.
     *
     * @return the immediate lower Resolution to this.
     */
    Optional<Resolution> getLower();

    /**
     * Rounds the given Instant DOWN to this Resolution.
     *
     * @param aInstant the date/time to be rounded down.
     * @return the given Instant rounded down to this Resolution.
     */
    Instant roundDown(Instant aInstant);

    /**
     * Rounds the given Instant UP to this Resolution. If the Instant lies precisely
     * on the boundary of the resolution, the result will be the same as the given
     * value.
     *
     * @param aInstant the date/time to be rounded up.
     * @return the given Instant rounded up to this Resolution.
     */
    Instant roundUp(Instant aInstant);

    /**
     * Returns the Instant immediately following the given Instant, at this Resolution.
     *
     * @param aInstant the given Instant.
     * @return the next canonical Instant at this Resolution.
     */
    Instant next(Instant aInstant);
}

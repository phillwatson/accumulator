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
package com.hillayes.accumulator.resolutions;

import com.hillayes.accumulator.Resolution;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Optional;

/**
 * Represents units of time at which data is accumulated and viewed. The units
 * are ordered in ascending order and each unit is an exact division of its
 * higher order unit.
 */
public enum DefaultResolution implements Resolution {
    MINUTE(null, ChronoUnit.MINUTES),
    HOUR(MINUTE, ChronoUnit.HOURS),
    DAY(HOUR, ChronoUnit.DAYS),
    WEEK(DAY, ChronoUnit.WEEKS);

    /**
     * An offset applied to the WEEK resolution calculations, to allow for the
     * fact that the epoch was a Thursday. This will round the time to the start
     * of the week (Monday).
     */
    private final static long WEEK_OFFSET = 345600; // 4 days in seconds

    private final Optional<Resolution> lower;
    private final TemporalUnit units;

    DefaultResolution(DefaultResolution aLower,
                      TemporalUnit aUnits) {
        lower = Optional.ofNullable(aLower);
        units = aUnits;
    }

    /**
     * Returns the Resolution that is the immediate lower resolution of this one.
     * If this is the lowest resolution, the result will be an empty Optional.
     *
     * @return the immediate lower resolution to this.
     */
    public Optional<Resolution> getLower() {
        return lower;
    }

    /**
     * Rounds the given Instant DOWN to this Resolution.
     *
     * @param aInstant the date/time to be rounded down.
     * @return the given Instant rounded to this Resolution.
     */
    public Instant roundDown(Instant aInstant) {
        if (this == WEEK) {
            long t = (aInstant.getEpochSecond() / 604800) * 604800;
            t += WEEK_OFFSET; // allow for the fact that the epoch was a Thursday
            return Instant.ofEpochSecond(t);
        }

        return aInstant.truncatedTo(units);
    }

    /**
     * Rounds the given Instant UP to this Resolution. If the Instant lies precisely
     * on the boundary of the resolution, the result will be the same as the given
     * value.
     *
     * @param aInstant the date/time to be rounded up.
     * @return the given Instant rounded up to this Resolution.
     */
    public Instant roundUp(Instant aInstant) {
        Instant result = roundDown(aInstant);
        if (result.equals(aInstant)) {
            return aInstant;
        }

        if (this == WEEK) {
            return result.plus(7, ChronoUnit.DAYS);
        }
        return result.plus(1, units);
    }

    /**
     * Returns the Instant immediately following the given Instant, at this Resolution.
     *
     * @param aInstant the given Instant.
     * @return the next canonical Instant.
     */
    public Instant next(Instant aInstant) {
        Instant result = roundDown(aInstant);

        if (this == WEEK) {
            return result.plus(7, ChronoUnit.DAYS);
        }
        return result.plus(1, units);
    }
}

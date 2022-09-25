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

/**
 * Identifies data that spans a specific date range, and whose natural ordering
 * is governed by that date range.
 * <p>
 * The date range covered is determined by a start and end date; where the start
 * date is inclusive and the end date is exclusive.
 * <pre>
 *   startDate >= n < endDate
 * </pre>
 */
public interface DateRangedData<T extends DateRangedData<T>> extends Comparable<T> {
    /**
     * Returns the start of the period covered by this instance, INCLUSIVE.
     */
    Instant getStartDate();

    /**
     * Returns the end of the period covered by this instance, EXCLUSIVE.
     */
    Instant getEndDate();

    /**
     * Compares the natural ordering of this instance with the given.
     *
     * @param aOther the instance to be compared.
     * @return -1 if this instance is earlier that the given, 0 if the two are
     * the same, and 1 if this instance is later than the given.
     */
    default int compareTo(T aOther) {
        if (aOther == null) {
            return 1;
        }

        int result = this.getStartDate().compareTo(aOther.getStartDate());
        if (result == 0) {
            result = this.getEndDate().compareTo(aOther.getEndDate());
        }
        return result;
    }
}

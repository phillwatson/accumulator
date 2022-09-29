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
package com.hillayes.accumulator.warehouse;

import com.hillayes.accumulator.Resolution;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class WarehouseRequest {
    private final int nameserver;
    private final Resolution resolution;
    private final Instant startDate;
    private final Instant endDate;

    /**
     * Divides this request into smaller units of the same resolution but no larger
     * than the given duration.
     *
     * @param aDuration the duration to which the request should be divided.
     * @return the list of requests that divide this request into smaller units of,
     * no more than, the given duration.
     */
    public List<WarehouseRequest> divide(Duration aDuration) {
        List<WarehouseRequest> result = new ArrayList<>();
        Instant s = startDate;
        while (s.isBefore(endDate)) {
            Instant e = s.plus(aDuration);
            if (e.isAfter(endDate)) {
                e = endDate;
            }

            result.add(WarehouseRequest.builder()
                .nameserver(nameserver)
                .resolution(resolution)
                .startDate(s)
                .endDate(e)
                .build());
            s = e;
        }

        return result;
    }
}

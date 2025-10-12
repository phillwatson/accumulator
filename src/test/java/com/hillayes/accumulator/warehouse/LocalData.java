package com.hillayes.accumulator.warehouse;

import com.hillayes.accumulator.DateRangedData;
import com.hillayes.accumulator.Resolution;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.Instant;

@Builder
@Data
@ToString
public class LocalData implements DateRangedData {
    private Resolution resolution;
    private Instant startDate;
    private Instant endDate;
    private long units;
    private long blocks;
}

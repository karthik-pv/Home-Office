package dev.jojo.HomeOffice.Entities;

import jakarta.persistence.Entity;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FundHouseXirrRequest {

    private String fundhouse;

    private List<String> schemes;

    private Double units;

    private Double nav;
}

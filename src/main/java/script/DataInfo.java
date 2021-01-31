package script;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataInfo {
    private String projectName;
    private int init;
    private int finish;
    private double radio;
    private double queryNumber;
    private String contain;
}

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "tool")
public class Tool {
    private @Id
    @Column(name = "code", unique = true)
    final String code;

    @Column(name = "type")
    private final String type;

    @Column(name = "brand")
    private final String brand;

    public Tool() {}

    public Tool(String code, String type, String brand) {
        this.code = code;
        this.type = type;
        this.brand = brand;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    @Override
    public String toString() {
        return String.format("Tool: {code=\"%s\", type=\"%s\", brand=\"%s\"}",
                this.code, this.type, this.brand);
    }
}

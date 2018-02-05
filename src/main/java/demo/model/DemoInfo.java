package demo.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Snart Lu on 2018/2/5. <br/>
 */
public class DemoInfo {
    private SimpleIntegerProperty type = new SimpleIntegerProperty();
    private SimpleStringProperty name = new SimpleStringProperty();
    private SimpleStringProperty description = new SimpleStringProperty();


    public int getType() {
        return type.get();
    }

    public SimpleIntegerProperty typeProperty() {
        return type;
    }

    public void setType(int type) {
        this.type.set(type);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }
}

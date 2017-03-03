package structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class StructureLite implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id_;
    public List<ModelLite> models = new ArrayList<>();

    public StructureLite(String id) {
        this.id_ = id;
    }

    public int numberOfModels() {
        return models.size();
    }

    public String getId() {
        return id_;
    }

    public void add(ModelLite model) {
        models.add(model);
    }

    public List<ModelLite> getModels() {
        return models;
    }

}

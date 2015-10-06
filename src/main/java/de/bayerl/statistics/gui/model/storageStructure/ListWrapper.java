package de.bayerl.statistics.gui.model.storageStructure;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "transformations")
public class ListWrapper {

    private List<TransformationModel> transformations = null;

    public List<TransformationModel> getTransformations() {
        return transformations;
    }

    @XmlElement(name = "transformation")
    public void setTransformations(List<TransformationModel> transformations) {
        this.transformations = transformations;
    }
}

package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Table;

public class AddMetadata extends Transformation {

    private String label;
    private String description;
    private String importer;

    public AddMetadata(String label, String description, String importer) {
        this.label = label;
        this.description = description;
        this.importer = importer;
    }

    @Override
    public String getName() {
        return "addMetadata";
    }

    @Override
    protected Table transformStep(Table table) {
        table.getMetadata().setLabel(label);
        table.getMetadata().setDescription(description);
        table.getMetadata().setImporter(importer);

        return table;
    }
}

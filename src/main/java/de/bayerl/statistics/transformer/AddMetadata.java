package de.bayerl.statistics.transformer;

import de.bayerl.statistics.model.Table;

/**
 * Adds external metadata to the table, which are not already part of the table.
 */
public class AddMetadata extends Transformation {

    private String label;
    private String description;
    private String importer;

    public AddMetadata(@NameAnnotation(name = "label") String label, @NameAnnotation(name = "description") String description, @NameAnnotation(name = "importer") String importer) {
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

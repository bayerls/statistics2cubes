package de.bayerl.statistics.transformer;


import de.bayerl.statistics.model.Table;

/**
 * Meta-transformation. E.g. adding line numbers.
 */
public interface MetaTransformation {
    Table transform(Table table);
}

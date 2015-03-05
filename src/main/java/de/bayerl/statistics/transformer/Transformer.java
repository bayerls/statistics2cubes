package de.bayerl.statistics.transformer;


import de.bayerl.statistics.model.Table;

public interface Transformer {


    Table transform(Table table);
}

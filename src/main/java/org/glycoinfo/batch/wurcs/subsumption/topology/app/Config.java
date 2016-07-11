package org.glycoinfo.batch.wurcs.subsumption.topology.app;

import org.glycoinfo.batch.glyconvert.ConvertSelectSparql;
import org.glycoinfo.batch.glyconvert.GlyConvertSparqlItemConfig;
import org.glycoinfo.convert.GlyConvert;
import org.glycoinfo.convert.GlyConvertConfig;
import org.glycoinfo.convert.wurcs.WurcsToWurcsTopologyConverter;
import org.glycoinfo.rdf.InsertSparql;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.dao.virt.VirtSesameTransactionConfig;
import org.glycoinfo.rdf.glycan.Glycosidic_topologyInsertSparql;
import org.glycoinfo.rdf.service.impl.GlycanProcedureConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import({VirtSesameTransactionConfig.class, GlycanProcedureConfig.class, GlyConvertConfig.class, ProcessorConfig.class})
public class Config {

	// to set the graph to insert into.
	public static String graph = "http://rdf.glytoucan.org/topology";

	@Primary
	@Bean(name="org.glycoinfo.batch.glyconvert")
	GlyConvert getGlyConvert() {
		return new WurcsToWurcsTopologyConverter();
	}
	
	@Bean(name = "itemReaderSelectSparql")
	SelectSparql itemReaderSelectSparql() {
		SelectSparql select = new TopologyConvertSelectSparql();
		select.setFrom("FROM <http://rdf.glytoucan.org/core> FROM <http://rdf.glytoucan.org/sequence/wurcs> FROM <http://rdf.glytoucan.org/topology>");
		select.setOrderBy(ConvertSelectSparql.AccessionNumber);
		return select;
	}

	@Bean(name = "itemWriterInsertSparql")
	InsertSparql getInsertSparql() {
	  Glycosidic_topologyInsertSparql convert = new Glycosidic_topologyInsertSparql();
		convert.setGraph(graph);
		return convert;
	}
}

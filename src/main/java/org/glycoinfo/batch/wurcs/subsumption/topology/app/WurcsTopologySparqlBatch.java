package org.glycoinfo.batch.wurcs.subsumption.topology.app;

import org.glycoinfo.convert.GlyConvertConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
public class WurcsTopologySparqlBatch {
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		ApplicationContext ctx = SpringApplication.run(WurcsTopologySparqlBatch.class, args);
	}
}
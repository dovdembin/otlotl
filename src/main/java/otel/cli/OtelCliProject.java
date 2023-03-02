package otel.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.EnumUtils;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;


@Command(name = "otel-cli", mixinStandardHelpOptions = true, version = "1.0.0", description = "java-cli")

public class OtelCliProject implements Callable<Integer> {
	private ExampleConfiguration openTelemetrySdkObj;
	
	enum Signals {
		TRACE, METRIC, LOG, BAGGAGE
	}

	enum Metrics {
		COUNTER, LONGUPDOWNCOUNTER, LONGGAUGE, LONGHISTOGRAM
	}

	@Option(names = { "-e", "--endpoint" }, description = "the endpoint of the collector or apm-server")
	String endpoint;

	@Option(names = { "-sig", "--signal" }, description = "which signal taces, meteric and so...")
	String signal;

	@Option(names = { "-m",	"--metrics" }, description = "which metrics to use could be counter, LongHistogram and so..")
	String metrics;

	@Option(names = { "-n",	"--metricName" }, description = "metric name")
	String metricName;
	
	@Option(names = { "-a", "--value" }, description = "example: -a name=test.bpt")
	Map<String, String> values;

	@Option(names = { "-i", "--ivalue" }, description = "example: -i value=1")
	Map<String, Integer> ivalues;

	@Option(names = { "-l", "-labels" }, description = "xpool labels")
	String labels;
	
	@Override
	public Integer call() throws Exception {
		try {
			if (Optional.ofNullable(endpoint).isEmpty() || Optional.ofNullable(signal).isEmpty() || Optional.ofNullable(metrics).isEmpty()) {
				Optional.ofNullable(endpoint).ifPresentOrElse(endpoint -> System.out.println(endpoint), () -> {
					System.out.println("Missing [endpoint]");
				});
				Optional.ofNullable(signal).ifPresentOrElse(signal -> System.out.println(signal), () -> {
					System.out.println("Missing [signal]");
				});
				Optional.ofNullable(metrics).ifPresentOrElse(metrics -> System.out.println(metrics), () -> {
					System.out.println("Missing [metrics]");
				});
				return 1;
			}
			if(!EnumUtils.isValidEnumIgnoreCase(Signals.class, signal)){
				System.out.println(signal + " not in list of Signals");
				return 1;
			}
			if(!EnumUtils.isValidEnumIgnoreCase(Metrics.class, metrics)){
				System.out.println(metrics + " not in list of metrics");
				return 1;
			}
			EnumUtils.isValidEnum(Metrics.class, metrics);
			// initialize
			openTelemetrySdkObj = new ExampleConfiguration();
			Map<String, String> otelMap = new HashMap<>();
			otelMap.put("endpoint", endpoint);
			openTelemetrySdkObj.initOpenTelemetry(otelMap);
			// handle signal
			switch (Signals.valueOf(signal.toUpperCase())) {
			case TRACE:
				// code block
				break;
			case METRIC:
				callMetrics();
				break;
			case LOG:
				// TODO
				break;
			default:
				// TODO
				System.out.println("nothing was choosed");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Optional.ofNullable(openTelemetrySdkObj).ifPresent(obj -> obj.closeOpenTelemetrySdk());
		}
		return 0;
	}

	private void callMetrics() {
		Meter meter = openTelemetrySdkObj.getOpenTelemetrySdk().meterBuilder("otel-cli").setInstrumentationVersion("1.0.0").build();
		switch (Metrics.valueOf(metrics.toUpperCase())) {
		case COUNTER:
			counterMetric(meter);
			break;
		case LONGUPDOWNCOUNTER:
			// TODO
			break;
		case LONGGAUGE:
			// TODO
			break;
		case LONGHISTOGRAM:
			// TODO
			break;
		default:
			// TODO
			System.out.println("nothing was choosed");
		}
	}

	private void counterMetric(Meter meter) {
		try {
			LongCounter counter = meter.counterBuilder(Optional.ofNullable(metricName).orElse("default-counter-name")).setDescription("counter metric").setUnit("1").build();
			LongHistogram histogram = meter.histogramBuilder("ddd").setDescription("show historgram of a label").ofLongs().build();
			AttributesBuilder attr = Attributes.builder();
			Optional.ofNullable(values).ifPresent(x -> x.entrySet().stream().forEach(m -> attr.put(m.getKey(), m.getValue())));
			Optional.ofNullable(ivalues).ifPresent(x -> x.entrySet().stream().forEach(m -> attr.put(m.getKey(), m.getValue())));
			Optional.ofNullable(labels).ifPresent(x -> {
				Optional<Set<String>> lbs = UtilTool.handleLabels(labels);
				lbs.ifPresent(s -> s.stream().forEach(t -> histogram.record(1, Attributes.of(AttributeKey.stringKey("testLabel"), t.trim()))));
			});
			counter.add(1, attr.build());
			
		} catch (Exception e) {
			
		}
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new OtelCliProject()).execute(args);
		System.exit(exitCode);
	}

}

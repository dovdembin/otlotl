package otel.cli;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class ExampleConfiguration {
	private OpenTelemetrySdk openTelemetrySdk;
	
	public OpenTelemetrySdk getOpenTelemetrySdk() {
		return openTelemetrySdk;
	}

	public void setOpenTelemetrySdk(OpenTelemetrySdk openTelemetrySdk) {
		this.openTelemetrySdk = openTelemetrySdk;
	}

	public void initOpenTelemetry(Map<String, String> map) {
		
		Resource resource = Resource.getDefault().merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "otel-cli-java")));

		
		
//		OtlpGrpcSpanExporter jaegerOtlpExporter = OtlpGrpcSpanExporter.builder()
//	            .setEndpoint(map.get("endpoint").toString())
//	            .setTimeout(30, TimeUnit.SECONDS)
//	            .build();
		
		OtlpGrpcMetricExporter metricOtlp =	OtlpGrpcMetricExporter.builder()
	            .setEndpoint(map.get("endpoint").toString())
	            .setTimeout(30, TimeUnit.SECONDS)
	            .build();
				 

//		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
//				  .addSpanProcessor(BatchSpanProcessor.builder(jaegerOtlpExporter).build())
//				  .setResource(resource)
//				  .build();
		
				SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
				  .registerMetricReader(PeriodicMetricReader.builder(metricOtlp).build())
				  .setResource(resource)
				  .build();

				openTelemetrySdk = OpenTelemetrySdk.builder()
				  .setMeterProvider(sdkMeterProvider)
				  .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
				  .buildAndRegisterGlobal();	 
	}
	
	public void closeOpenTelemetrySdk() {
		Optional.ofNullable(openTelemetrySdk).ifPresent(x->openTelemetrySdk.close());
	}
}

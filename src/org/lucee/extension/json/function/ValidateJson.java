package org.lucee.extension.json.function;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingMessage;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.JsonLoader;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Creation;

public class ValidateJson implements Function {

	private static final long serialVersionUID = -8114568696008052170L;

	public static final String KEY_ISVALID = "IsValid";
	public static final String KEY_ERRORS = "Errors";

	public static Struct call(PageContext pc, Object oJson, Object oSchema) throws PageException {
		return call(pc, oJson, oSchema, false);
	}

	public static Struct call(PageContext pc, Object oJson, Object oSchema, boolean throwOnError) throws PageException {
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		Creation creation = engine.getCreationUtil();

		Struct result = creation.createStruct();
		Array errors = creation.createArray();
		ProcessingReport report;
		try {

			JsonNode jnDoc = toJsonNode(pc, engine, oJson);
			JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
			JsonSchema jsSchema = factory.getJsonSchema(toJsonNode(pc, engine, oSchema));
			report = jsSchema.validate(jnDoc);
		} catch (Throwable t) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(t);
		}
		result.put(KEY_ERRORS, errors);

		if (report != null && report.isSuccess()) {
			result.put(KEY_ISVALID, Boolean.TRUE);
		} else {
			result.put(KEY_ISVALID, Boolean.FALSE);
			if (report != null) {
				for (ProcessingMessage message : report) {
					if (throwOnError)
						throw engine.getCastUtil().toPageException(message.asException());
					errors.append(toStruct(pc, engine, message.asJson()));
				}
			}
		}

		return result;
	}

	private static Object toStruct(PageContext pc, CFMLEngine engine, JsonNode jn) throws PageException {
		try {
			BIF bif = engine.getClassUtil().loadBIF(pc, "lucee.runtime.functions.conversion.DeserializeJSON"); // TODO
																												// don't
																												// use
																												// this
			return bif.invoke(pc, new Object[] { jn.toString() });
		} catch (Exception e) {
			throw engine.getCastUtil().toPageException(e);
		}
	}

	private static JsonNode toJsonNode(PageContext pc, CFMLEngine engine, Object oJson) throws PageException {
		if (engine.getDecisionUtil().isStruct(oJson)) {
			Struct sct = engine.getCastUtil().toStruct(oJson);
			try {
				BIF bif = engine.getClassUtil().loadBIF(pc, "lucee.runtime.functions.conversion.SerializeJSON"); // TODO
																													// don't
																													// use
																													// this
				return JsonLoader.fromString(engine.getCastUtil().toString(bif.invoke(pc, new Object[] { sct })));
			} catch (Exception e) {
				throw engine.getCastUtil().toPageException(e);
			}
		}

		try {
			return JsonLoader.fromString(engine.getCastUtil().toString(oJson));
		} catch (IOException e) {
			throw engine.getCastUtil().toPageException(e);
		}
	}
}
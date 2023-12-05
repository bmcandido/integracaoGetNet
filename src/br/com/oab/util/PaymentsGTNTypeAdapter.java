package br.com.oab.util;

class PaymentsGTNTypeAdapter {
	
	String teste;
}

//package br.com.oab.util;
//
//import com.google.gson.*;
//import java.lang.reflect.Type;
//
//import br.com.oab.model.GetnetPmtOrderModel.PaymentsGTN;
//
//import br.com.oab.model.GetnetPmtOrderModel.PaymentsGTN.TransactionGTN;
//
//public class PaymentsGTNTypeAdapter implements JsonSerializer<PaymentsGTN>, JsonDeserializer<PaymentsGTN> {
//
////	@Override
////	public JsonElement serialize(PaymentsGTN src, Type typeOfSrc, JsonSerializationContext context) {
////
////		return null;
////	}
////
////	@Override
////	public PaymentsGTN deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
////			throws JsonParseException {
////		JsonObject jsonObject = json.getAsJsonObject();
////		PaymentsGTN paymentsGTN = new PaymentsGTN();
////		paymentsGTN.setRegistredAt(jsonObject.get("registred_at").getAsString());
////		paymentsGTN.setStatus(jsonObject.get("status").getAsString());
////
////		String paymentType = jsonObject.get("payment_type").getAsString();
////		if (paymentType.equals("DEBIT")) {
////			paymentsGTN.setTransaction(context.deserialize(jsonObject.get("transaction"), DebitGNT.class));
////		} else if (paymentType.equals("CREDIT")) {
////			paymentsGTN.setTransaction(context.deserialize(jsonObject.get("transaction"), TransactionGTN.class));
////		} else {
////		
////		}
////
////		return paymentsGTN;
////	}
//
//}

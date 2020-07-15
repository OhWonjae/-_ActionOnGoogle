package com.o2o.action.server.app;


import com.google.actions.api.*;
import com.google.actions.api.Capability;
import com.google.actions.api.response.ResponseBuilder;
import com.google.actions.api.response.helperintent.SelectionCarousel;
import com.google.actions.api.response.helperintent.SelectionList;
import com.google.api.services.actions_fulfillment.v2.model.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.o2o.action.server.util.CommonUtil;
//import com.sun.org.apache.xpath.internal.operations.String;
import sun.rmi.runtime.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URLEncoder;
import java.lang.String;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class internApp extends DialogflowApp {
	public static void main(String[] args) {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		try
		{
			String currentPage = "1";
			String countPerPage = "100";
			String resultType = "json";
			String confmKey = "devU01TX0FVVEgyMDIwMDcwOTE3MDUyMDEwOTk0MjQ=";
			String keyword = "강원도";
			String URLkeyword = "";
			try {
				URLkeyword = URLEncoder.encode(keyword, "UTF-8");
			} catch (UnsupportedEncodingException ec) {
				ec.printStackTrace();
			}

			final String apiUrl = "http://www.juso.go.kr/addrlink/addrLinkApi.do?currentPage=" + currentPage
					+ "&countPerPage=" + countPerPage + "&keyword=" + URLkeyword
					+ "&confmKey=" + confmKey + "&resultType=" + resultType;
			apiController api = new apiController();
			String result = api.get(apiUrl);
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonobj = (JsonObject) jsonParser.parse(result);
			String errorCode = jsonobj.get("results").getAsJsonObject().get("common").getAsJsonObject().get("errorCode").getAsString();
			if(!errorCode.equals("0"))
			{
				System.out.println(errorCode);
			}

		}catch(Exception e)
		{
			e.printStackTrace();
		}finally
		{
			try {
				if(br!=null)
				br.close();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}


	}

	@ForIntent("Default Welcome Intent")
	public ActionResponse defaultWelcome(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();

		data.clear();

		/*List<String> suggestions = new ArrayList<String>();
		SimpleResponse simpleResponse = new SimpleResponse();
		simpleResponse.setTextToSpeech("안녕하세요, 테스트앱 입니다.")
				.setDisplayText("안녕하세요, 테스트앱 입니다.");
			rb.add(simpleResponse);*/
		return rb.build();
	}
	/*public ActionResponse RetError(ActionRequest request, String errorCode) throws ExecutionException, InterruptedException {
	{
		ResponseBuilder responseBuilder = getResponseBuilder(request);

		SimpleResponse simpleResponse = new SimpleResponse();
		simpleResponse.setTextToSpeech(errorCode)
				.setDisplayText(errorCode);
		responseBuilder.add(simpleResponse);
		return responseBuilder.build();
	}*/
	ArrayList<ListSelectListItem> ItemList;
	@ForIntent("AdressConfirm")
	public ActionResponse confirmAdress(ActionRequest request) throws ExecutionException, InterruptedException {
				ResponseBuilder responseBuilder = getResponseBuilder(request);
				if (!request.hasCapability(Capability.SCREEN_OUTPUT.getValue())) {
					return responseBuilder
							.add("Sorry, try ths on a screen device or select the phone surface in the simulator.")
							.add("Which response would you like to see next?")
							.build();
		}
		// 사용자가 적은 주소
		String adress = request.getRawText();

		String currentPage = "1";
		String countPerPage = "5";
		String resultType = "json";
		String confmKey = "devU01TX0FVVEgyMDIwMDcwOTE3MDUyMDEwOTk0MjQ=";
		String keyword = adress;
		String URLkeyword = "";
		try {
			URLkeyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException ec) {
			ec.printStackTrace();
		}

		final String apiUrl = "http://www.juso.go.kr/addrlink/addrLinkApi.do?currentPage=" + currentPage
				+ "&countPerPage=" + countPerPage + "&keyword=" + URLkeyword
				+ "&confmKey=" + confmKey + "&resultType=" + resultType;
		apiController api = new apiController();
		String result = api.get(apiUrl);
// 가장 큰 JSONObject를 가져옵니다.
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonobj = (JsonObject) jsonParser.parse(result);
		String errorCode = jsonobj.get("results").getAsJsonObject().get("common").getAsJsonObject().get("errorCode").getAsString();
		String errorMessage = jsonobj.get("results").getAsJsonObject().get("common").getAsJsonObject().get("errorMessage").getAsString();
		if(!errorCode.equals("0"))
		{
			SimpleResponse simpleResponse = new SimpleResponse();
			simpleResponse.setTextToSpeech(errorMessage + "다시 입력해 주세요.")
					.setDisplayText(errorMessage + "다시 입력해 주세요.");
			responseBuilder.add(simpleResponse);
			return responseBuilder.build();
		}
			JsonElement jsone = jsonobj.get("results").getAsJsonObject().get("juso");
			String st = "";
			List AdressList = new ArrayList();
			ItemList = new ArrayList<>();
			String Spagenumber = jsonobj.get("results").getAsJsonObject().get("common").getAsJsonObject().get("totalCount").getAsString();
			if(Integer.parseInt(Spagenumber)==1)
			{
				JsonObject obj = (JsonObject) jsone.getAsJsonArray().get(0);
				responseBuilder
						.add("검색 결과입니다.")
						.add(
								new BasicCard()
										.setTitle("지번주소 : "+obj.get("jibunAddr").toString()+"\n우편번호 : " + obj.get("zipNo").toString())
										.setFormattedText("도로명 주소 : "+obj.get("roadAddr").toString())
										.setButtons(
												new ArrayList<Button>(
														Arrays.asList(
																new Button()
																		.setTitle("지도로 이동")
																		.setOpenUrlAction(
																				new OpenUrlAction().setUrl("https://map.kakao.com/link/search/"+obj.get("roadAddr")))))));
				return responseBuilder.build();
			}
			for (int i = 0; i <jsone.getAsJsonArray().size(); i++) {
				JsonObject obj = (JsonObject) jsone.getAsJsonArray().get(i);
				ListSelectListItem Item  = new ListSelectListItem();
				Item.setDescription("지번주소 : "+obj.get("jibunAddr").toString()+"\n우편번호 : " + obj.get("zipNo").toString());
				Item.setTitle("도로명 주소 : "+obj.get("roadAddr").toString());
				OptionInfo optionInfo = new OptionInfo();
				optionInfo.setKey(Integer.toString(i));
				Item.setOptionInfo(optionInfo);
				AdressList.add(Item);
				ItemList.add(Item);
			}
			responseBuilder
					.add("원하시는 도로명 주소를 선택해 주세요." + Spagenumber + " " +jsone.getAsJsonArray().size() )
					.add(
							new SelectionList()
									.setTitle("도로명 주소 리스트")
									.setItems(AdressList));
/*		SimpleResponse simpleResponse = new SimpleResponse();
		simpleResponse.setTextToSpeech(Integer.toString(jsone.getAsJsonArray().size()))
				.setDisplayText(Integer.toString(jsone.getAsJsonArray().size()));
		responseBuilder.add(simpleResponse);*/
			return responseBuilder.build();
		}
		@ForIntent("ListSelection")
		public ActionResponse listSelected (ActionRequest request){
			ResponseBuilder responseBuilder = getResponseBuilder(request);

			String selectedItem = request.getSelectedOption();
			int ItemIndex = Integer.parseInt(selectedItem);
			ListSelectListItem item  = ItemList.get(ItemIndex);
			// a line break to be rendered in the card.
			responseBuilder
					.add("검색 결과입니다.")
					.add(
							new BasicCard()
									.setTitle(item.getDescription())
									.setFormattedText(item.getTitle())
									.setButtons(
											new ArrayList<Button>(
													Arrays.asList(
															new Button()
																	.setTitle("지도로 이동")
																	.setOpenUrlAction(
																			new OpenUrlAction().setUrl("https://map.kakao.com/link/search/"+item.getTitle()))))));


			return responseBuilder.build();
		}


}


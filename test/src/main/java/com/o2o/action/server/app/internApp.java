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
import sun.java2d.pipe.SpanShapeRenderer;
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
	//테스트 Main문
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
	//서류마음이 이미지 url1
	String MaumImageUrl = "https://mblogthumb-phinf.pstatic.net/MjAxOTExMThfMjAy/MDAxNTc0MDU0MDU3MzI3.pSSabICxa-7KDC8e0Kbe1zsPRGrT14qiuvjwKtBsAjkg.5xUHN3AwhVFYSUDY6SAq2gfVHCBPA1T7dGZAMFJBi7Yg.GIF.gbdc04/2.gif?type=w800";
	//서류마음이 이미지 url2
	String MaumQImageUrl = "https://mblogthumb-phinf.pstatic.net/MjAxOTExMThfODMg/MDAxNTc0MDU1MTE2NzU3.HSMUvYeZLKHjSoBIXutV6aNGjLECFVYgZxiKQG7OLhUg.J3pu0ztBk1yqgGOmMiDpTJUxbcvjLwY-83DAIbFwGrcg.GIF.gbdc04/emot_011_x3.gif?type=w800";
	@ForIntent("Default Welcome Intent")
	public ActionResponse defaultWelcome(ActionRequest request) throws ExecutionException, InterruptedException {
		ResponseBuilder rb = getResponseBuilder(request);
		Map<String, Object> data = rb.getConversationData();

		data.clear();
		return rb.build();
	}

	//AdressConfirm 인텐트에서 받은 아이템리스트 담을 전역변수
	ArrayList<ListSelectListItem> ItemList;

	//입력된 주소와 매칭할 인텐트
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

		String currentPage = "1"; //보여줄 페이지
		String countPerPage = "100"; //페이지 당 보여줄 목록 갯수
		String resultType = "json";
		String confmKey = "devU01TX0FVVEgyMDIwMDcwOTE3MDUyMDEwOTk0MjQ=";
		String keyword = adress;
		String URLkeyword = "";
		try {
			//한글 인식 위해 UTF-8 추가
			URLkeyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException ec) {
			ec.printStackTrace();
		}

		final String apiUrl = "http://www.juso.go.kr/addrlink/addrLinkApi.do?currentPage=" + currentPage
				+ "&countPerPage=" + countPerPage + "&keyword=" + URLkeyword
				+ "&confmKey=" + confmKey + "&resultType=" + resultType;
		//Get 함수를 통한 RestApi 정보 받아오기
		apiController api = new apiController();
		String result = api.get(apiUrl);
		//받아온 Json정보 파싱
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonobj = (JsonObject) jsonParser.parse(result);
		// 받아온 정보의 에러코드 확인
		String errorCode = jsonobj.get("results").getAsJsonObject().get("common").getAsJsonObject().get("errorCode").getAsString();
		String errorMessage = jsonobj.get("results").getAsJsonObject().get("common").getAsJsonObject().get("errorMessage").getAsString();
		// 에러코드가 정상이 아니면 에러 메시지 출력
		if(!errorCode.equals("0"))
		{
			responseBuilder
					//에러 메시지 출력
					.add("<speak>\n" +
							"  <audio src=\"https://actions.o2o.kr/devsvr3/Image/Response_API_Error.mp3\">\n" +
							"  <desc>"+errorMessage+"</desc>\n</audio>\n" +
							"</speak>")
					.add(
							new BasicCard()
									//서류마음이 이미지 추가
									.setImage(
											new Image()
													.setUrl(MaumQImageUrl)
													.setAccessibilityText("서류마음이")));
			return responseBuilder.build();
		}

			JsonElement jsone = jsonobj.get("results").getAsJsonObject().get("juso");
			String st = "";
			List AdressList = new ArrayList();
			//주소를 받아올때마다 ItemList 초기화
			ItemList = new ArrayList<>();
			String Spagenumber = jsonobj.get("results").getAsJsonObject().get("common").getAsJsonObject().get("totalCount").getAsString();
			//받아온 결과주소가 0개면 메시지 출력
			if(Integer.parseInt(Spagenumber)==0) {
				responseBuilder
						.add("<speak>\n" +
								"  <audio src=\"https://actions.o2o.kr/devsvr3/Image/Response_NoResult.mp3\">\n" +
								"  <desc>검색 결과가 없습니다. 다시 입력해 주세요.</desc>\n</audio>\n" +
								"</speak>")
				.add(
						new BasicCard()
								//서류마음이 이미지 추가
								.setImage(
										new Image()
												.setUrl(MaumQImageUrl)
												.setAccessibilityText("서류마음이")));
				return responseBuilder.build();
			}

			//받아온 결과주소가 1개면 베이직카드로 출력 후 해당 인텐트 종료
			if(Integer.parseInt(Spagenumber)==1)
			{
				JsonObject obj = (JsonObject) jsone.getAsJsonArray().get(0);
				responseBuilder
						.add(
								new BasicCard()
										//서류마음이 이미지 추가
										.setImage(
												new Image()
														.setUrl(MaumImageUrl)
														.setAccessibilityText("서류마음이"))
										//내가 입력한 지번주와 대조 위해 완전한 지번주소를 타이틀로, 도로명주소를 내용으로 작성
										.setTitle("지번주소 : "+obj.get("jibunAddr").toString()+"\n우편번호 : " + obj.get("zipNo").toString())
										.setFormattedText("도로명 주소 : "+obj.get("roadAddr").toString())
										.setButtons(
												new ArrayList<Button>(
														Arrays.asList(
																new Button()
																		.setTitle("지도로 이동")
																		.setOpenUrlAction( //베이직카드의 지도로 이동 버튼누르면 KakaoAPI의 지도 보기로 연결
																				new OpenUrlAction().setUrl("https://map.kakao.com/link/search/"+obj.get("roadAddr")))))));
				return responseBuilder.build();
			}
			// 리스트로 뿌릴 아이템 만든 후 AdressList와 ItemList에 추가
			for (int i = 0; i <jsone.getAsJsonArray().size(); i++) {
				JsonObject obj = (JsonObject) jsone.getAsJsonArray().get(i);
				ListSelectListItem Item  = new ListSelectListItem();
				Item.setDescription("지번주소 : "+obj.get("jibunAddr").toString()+"\n우편번호 : " + obj.get("zipNo").toString());
				Item.setTitle("도로명 주소 : "+obj.get("roadAddr").toString());
				OptionInfo optionInfo = new OptionInfo();
				//옵션키는 받아온 리스트의 인덱스로 할당
				optionInfo.setKey(Integer.toString(i));
				Item.setOptionInfo(optionInfo);
				AdressList.add(Item);
				ItemList.add(Item);
			}

			//리스트 리턴
			responseBuilder
					.add("<speak>\n" +
							"  <audio src=\"https://actions.o2o.kr/devsvr3/Image/Response_ListSelect.mp3\">\n" +
							"  <desc>원하시는 도로명 주소를 선택해 주세요.</desc>\n</audio>\n" +
							"</speak>")
					.add(
							new SelectionList()
									.setTitle("도로명 주소 리스트")
									.setItems(AdressList));
			return responseBuilder.build();
		}

		// AdressConfirm 인텐트에서 받아온 리스트를 선택했을때 매칭되는 인텐트
		@ForIntent("ListSelection")
		public ActionResponse listSelected (ActionRequest request){
			ResponseBuilder responseBuilder = getResponseBuilder(request);
			//리스트의 option key로 설정해둔 인덱스를 가져와서 ItemList에서 해당 인덱스를 이용하여 선택된 아이템 뽑아내기
			String selectedItem = request.getSelectedOption();
			int ItemIndex = Integer.parseInt(selectedItem);
			ListSelectListItem item  = ItemList.get(ItemIndex);
			//선택된 정보는 베이직카드로 리턴
			responseBuilder
					.add(new SimpleResponse().setTextToSpeech(item.getTitle()).setDisplayText(""))
					.add(
							new BasicCard()
									//서류마음이 이미지 추가
									.setImage(
											new Image()
													.setUrl(MaumImageUrl)
													.setAccessibilityText("서류마음이"))
									//내가 입력한 지번주와 대조 위해 완전한 지번주소를 타이틀로, 도로명주소를 내용으로 작성
									.setTitle(item.getDescription())
									.setFormattedText(item.getTitle())
									.setButtons(
											new ArrayList<Button>(
													Arrays.asList(
															new Button()
																	.setTitle("지도로 이동")
																	.setOpenUrlAction(//베이직카드의 지도로 이동 버튼누르면 KakaoAPI의 지도 보기로 연결
																			new OpenUrlAction().setUrl("https://map.kakao.com/link/search/"+item.getTitle()))))));

			return responseBuilder.build();
		}


}


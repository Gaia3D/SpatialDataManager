package com.gaia3d.tadpole.spatial.data.core.preference.data;

import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.session.manager.SessionManager;
import com.hangum.tadpole.sql.dao.system.UserInfoDataDAO;
import com.hangum.tadpole.sql.query.TadpoleSystemInitializer;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * update user preference
 * 
 * @author hangum
 *
 */
public class SpatialGetPreferenceData {

	/** 지도를 그리기위해 데이터를 지도쪽에 데이터를 보내는 양. */
	public static int getSendMapDataCount() {
		UserInfoDataDAO userInfo = SessionManager.getUserInfo(SpatialPreferenceDefine.SEND_MAP_DATA_COUNT);
		if(userInfo == null) return Integer.parseInt(SpatialPreferenceDefine.SEND_MAP_DATA_COUNT_VALUE);
		return Integer.parseInt(userInfo.getValue0());
	}
	
	/** 사용자가 지도를 클릭했을때 선택되는 색. */
	public static String getUserClickedColor() {
		UserInfoDataDAO userInfo = SessionManager.getUserInfo(SpatialPreferenceDefine.USER_CLICKED_COLOR);
		if(userInfo == null) return SpatialPreferenceDefine.USER_CLICKED_COLOR_VALUE;
		return userInfo.getValue0();
	}
	
	
	/**
	 * 신규 사용자의 기본 유저 데이터 정보를 저장합니다.
	 * 
	 * @param sendMapDataCount 
	 * @param userClickedColor
	 */
	public static void updatePreferenceData(String sendMapDataCount, String userClickedColor) throws Exception {
		SqlMapClient sqlClient = TadpoleSQLManager.getInstance(TadpoleSystemInitializer.getUserDB());
		UserInfoDataDAO userInfoData = new UserInfoDataDAO();
		userInfoData.setUser_seq(SessionManager.getSeq());
		
		// send map data count
		userInfoData.setName(SpatialPreferenceDefine.SEND_MAP_DATA_COUNT);
		userInfoData.setValue0(sendMapDataCount);
		sqlClient.update("userInfoDataUpdate", userInfoData); //$NON-NLS-1$
		
		// user clicked color
		userInfoData.setName(SpatialPreferenceDefine.USER_CLICKED_COLOR);
		userInfoData.setValue0(userClickedColor);
		sqlClient.update("userInfoDataUpdate", userInfoData); //$NON-NLS-1$
	}
}

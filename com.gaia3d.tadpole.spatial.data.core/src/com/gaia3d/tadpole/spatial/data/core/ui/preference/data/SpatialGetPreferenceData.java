/*******************************************************************************
 * Copyright 2014 hangum
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gaia3d.tadpole.spatial.data.core.ui.preference.data;

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
		UserInfoDataDAO userInfo = SessionManager.getUserInfo(SpatialPreferenceDefine.SPATIAL_SEND_MAP_DATA_COUNT);
		if(userInfo == null) return Integer.parseInt(SpatialPreferenceDefine.SPATIAL_SEND_MAP_DATA_COUNT_VALUE);
		return Integer.parseInt(userInfo.getValue0());
	}
	
	/** 사용자가 지도를 클릭했을때 선택되는 색. */
	public static String getUserOptions() {
		UserInfoDataDAO userInfo = SessionManager.getUserInfo(SpatialPreferenceDefine.SPATIAL_USER_OPTIONS);
		if(userInfo == null) return SpatialPreferenceDefine.SPATIAL_USER_OPTIONS_VALUE;
		return userInfo.getValue0();
	}
	
	/**
	 * 신규 사용자의 기본 유저 데이터 정보를 저장합니다.
	 * 
	 * @param sendMapDataCount 
	 */
	public static void updateSendMapDataCount(String sendMapDataCount) throws Exception {
		SqlMapClient sqlClient = TadpoleSQLManager.getInstance(TadpoleSystemInitializer.getUserDB());
		UserInfoDataDAO userInfoData = new UserInfoDataDAO();
		userInfoData.setUser_seq(SessionManager.getSeq());
		
		// send map data count
		userInfoData.setName(SpatialPreferenceDefine.SPATIAL_SEND_MAP_DATA_COUNT);
		userInfoData.setValue0(sendMapDataCount);
		sqlClient.update("userInfoDataUpdate", userInfoData); //$NON-NLS-1$
	}
	
	/**
	 * 신규 사용자의 기본 유저 데이터 정보를 저장합니다.
	 * 
	 * @param sendMapDataCount 
	 */
	public static void updateUserOptions(String userData) throws Exception {
		SqlMapClient sqlClient = TadpoleSQLManager.getInstance(TadpoleSystemInitializer.getUserDB());
		UserInfoDataDAO userInfoData = new UserInfoDataDAO();
		userInfoData.setUser_seq(SessionManager.getSeq());
		
		// send map data count
		userInfoData.setName(SpatialPreferenceDefine.SPATIAL_SEND_MAP_DATA_COUNT);
		userInfoData.setValue0(userData);
		sqlClient.update("userInfoDataUpdate", userInfoData); //$NON-NLS-1$
	}
}

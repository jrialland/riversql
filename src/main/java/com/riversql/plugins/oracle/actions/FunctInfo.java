
package com.riversql.plugins.oracle.actions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.riversql.IDManager;
import com.riversql.plugin.BasePluginType;
import com.riversql.plugins.oracle.FunctionNode;
import org.slf4j.LoggerFactory;


public class FunctInfo {
	String id;
	public FunctInfo(String id) {
		this.id=id;
	}

	public JSONObject execute()throws Exception {
		JSONObject results=new JSONObject();
		JSONArray meta=new JSONArray();
		JSONArray data=new JSONArray();
		String []strs={"Property","Value"};
		
		for(int i=0;i<strs.length;i++){
			meta.put(strs[i]);	
		}
		Object obj=IDManager.get().get(id);
		FunctionNode funz=(FunctionNode)obj;
		final String sql="SELECT  created,last_ddl_time, timestamp, status FROM sys.all_objects where owner=? and object_type='FUNCTION' and object_name=?";
		
		ResultSet rs=null;
		PreparedStatement ps=null;
  		try{
			ps=funz.getConn().prepareStatement(sql);
			BasePluginType tot=(BasePluginType)funz.getParent();
			String owner=tot.getParent().getName();
			ps.setString(1,owner);
			ps.setString(2,funz.getName());
			rs=ps.executeQuery();
			
			if(rs.next()){
				JSONArray record=new JSONArray();
				record.put("Created");
				record.put(rs.getString(1));
				data.put(record);
				
				record=new JSONArray();
				record.put("Last DDL Time");
				record.put(rs.getString(2));
				data.put(record);
				
				record=new JSONArray();
				record.put("TimeStamp");
				record.put(rs.getString(3));
				data.put(record);
				
				record=new JSONArray();
				record.put("Status");
				record.put(rs.getString(4));
				data.put(record);
				
			}
  		}catch(Exception e){
  			LoggerFactory.getLogger(getClass()).error("error", e);;
  		}finally{
  			try{if(rs!=null)
  				rs.close();
  			}catch(Exception e){
  			}
  			try{if(ps!=null)
  				ps.close();
  			}catch(Exception e){
  			}
  		}
		
  		results.put("meta",meta);
		results.put("data",data);
		return results;
	}

}

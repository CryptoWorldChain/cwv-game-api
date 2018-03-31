package org.brewchain.cwv.auth.filter;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@AllArgsConstructor
@NoArgsConstructor
public class SMSession {

//	TXTpsUser txtpsuser;

	String brokerage; 
	
	Date loginTime;

	Date lastTime;
	
	String loginname;
	
	List<String> orgs;
}

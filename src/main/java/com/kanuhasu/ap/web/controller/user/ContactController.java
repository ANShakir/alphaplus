package com.kanuhasu.ap.web.controller.user;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanuhasu.ap.business.bo.ResponseEntity;
import com.kanuhasu.ap.business.bo.user.ContactEntity;
import com.kanuhasu.ap.business.service.impl.user.ContactServiceImpl;
import com.kanuhasu.ap.business.type.response.Param;
import com.kanuhasu.ap.business.util.CommonUtil;
import com.kanuhasu.ap.business.util.SearchInput;

@CrossOrigin
@Controller
@RequestMapping("/contact")
public class ContactController implements ResourceLoaderAware {
	private static final Logger logger = Logger.getLogger(ContactController.class);
	@Autowired
	private ContactServiceImpl contactService;
	private ResourceLoader resourceLoader;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	// setter-getter
	
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity save(@RequestBody ContactEntity contact) {
		contact = contactService.save(contact);
		return ResponseEntity.builder().responseEntity(contact).build();
	}
	
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity update(@RequestBody ContactEntity contact) {
		contact = contactService.update(contact);
		return ResponseEntity.builder().responseEntity(contact).build();
	}
	
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public @ResponseBody ContactEntity get(@RequestParam("contactID") long contactID) {
		logger.info("Fetch contact for: [" + contactID + "]");
		ContactEntity contact = contactService.get(contactID);
		return contact;
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity search(@RequestBody SearchInput searchInput) throws ParseException {
		List<ContactEntity> contactList = contactService.search(searchInput);
		long rowCount = contactService.getTotalRowCount(searchInput);
		
		Map<String, String> respMap = new HashMap<String, String>();
		respMap.put(Param.ROW_COUNT.val(), String.valueOf(rowCount));
		respMap.put(Param.CURRENT_PAGE_NO.val(), String.valueOf(searchInput.getPageNo()));
		respMap.put(Param.TOTAL_PAGE_COUNT.val(), String.valueOf(CommonUtil.calculateNoOfPages(rowCount, searchInput.getRowsPerPage())));
		respMap.put(Param.ROWS_PER_PAGE.val(), String.valueOf(searchInput.getRowsPerPage()));
		
		ResponseEntity response = new ResponseEntity();
		response.setResponseData(respMap);
		response.setResponseEntity(contactList);
		
		return response;
	}
	
	// data
	
	/**
	 * http://localhost:8080/alphaplus/ctrl/client/getColumnData
	 * 
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getColumnData", method = RequestMethod.GET)
	public @ResponseBody List<Object> getColumnData() throws IOException {
		Resource addressColumnJson = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(CommonUtil.isAuth(auth)) {
			Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) auth.getAuthorities();
			if(CommonUtil.isAdmin(authorities)) {
				addressColumnJson = this.resourceLoader.getResource("classpath:data/json/contact/columnDataAdmin.json");
			}
			else {
				addressColumnJson = this.resourceLoader.getResource("classpath:data/json/contact/columnDataMember.json");
			}
		}
		List<Object> addressColumnData = objectMapper.readValue(addressColumnJson.getFile(), List.class);
		return addressColumnData;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getFormData", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getFormData() throws IOException {
		Resource clientFormData = this.resourceLoader.getResource("classpath:data/json/contact/formData.json");
		Map<String, Object> messageFormDataMap = objectMapper.readValue(clientFormData.getFile(), Map.class);
		return messageFormDataMap;
	}
}
/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.web;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.hr.db.EmployeeSalary;
import com.axelor.apps.hr.db.EmployeeSalaryLine;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;

@Singleton
public class EmployeeSalaryController {

  public void printEmployeeSalaryReport(ActionRequest request, ActionResponse response)
      throws AxelorException {
	  EmployeeSalary employeeSalary = request.getContext().asType(EmployeeSalary.class);

	    String idsStr = "";
	    List<Long> idList = new ArrayList<Long>();
	    for(EmployeeSalaryLine employeeSalaryLine : employeeSalary.getEmployeeSalaryRecord()) {
	    	idList.add(employeeSalaryLine.getId()); 
	    }
	    idsStr = Joiner.on(",").join(idList);

	    String fileLink =
	        ReportFactory.createReport("EmployeeSalary2.rptdesign", "Salary Report")
	            .addParam("ids", idsStr)
	            .generate()
	            .getFileLink();

	    response.setView(ActionView.define("Salary Report").add("html", fileLink).map());
	  
  }
}

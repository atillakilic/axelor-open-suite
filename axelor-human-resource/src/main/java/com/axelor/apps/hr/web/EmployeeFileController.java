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

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.axelor.apps.ReportFactory;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class EmployeeFileController {

  public void printExpeirationDateReport(ActionRequest request, ActionResponse response) throws AxelorException {
	  
	  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	  String startDateStr = (String) request.getContext().get("startDate");
	  String endDateStr = (String) request.getContext().get("endDate");

	  //convert String to LocalDate
	  LocalDate startDate = LocalDate.parse(startDateStr, formatter);
	  LocalDate endtDate = LocalDate.parse(endDateStr, formatter);

	        String fileLink =
              ReportFactory.createReport("EmployeeDocument.rptdesign", "Document Report")
                  .addParam("StartDate", Date.valueOf(startDate))
                  .addParam("EndDate", Date.valueOf(endtDate))
                  .generate()
                  .getFileLink();

          response.setView(ActionView.define("Document Report").add("html", fileLink).map());
	}
}

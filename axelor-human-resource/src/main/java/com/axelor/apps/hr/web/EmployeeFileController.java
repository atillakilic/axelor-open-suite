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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.hr.db.EmployeeFile;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Singleton
public class EmployeeFileController {

  public void printExpeirationDateReport(ActionRequest request, ActionResponse response)
      throws AxelorException {

    String idsStr = "";
    if (request.getContext().get("_ids") == null) {
      response.setAlert("Please select the grid.");
      return;
    }
    List<Integer> selectedFiels = (List<Integer>) request.getContext().get("_ids");
    idsStr = Joiner.on(",").join(selectedFiels);

    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // String startDateStr = (String) request.getContext().get("startDate");
    // String endDateStr = (String) request.getContext().get("endDate");
    //
    // // convert String to LocalDate
    // LocalDate startDate = LocalDate.parse(startDateStr, formatter);
    // LocalDate endtDate = LocalDate.parse(endDateStr, formatter);

    String fileLink =
        ReportFactory.createReport("EmployeeDocument.rptdesign", "Document Report")
            // .addParam("StartDate", Date.valueOf(startDate))
            // .addParam("EndDate", Date.valueOf(endtDate))
            .addParam("ids", idsStr)
            .generate()
            .getFileLink();

    response.setView(ActionView.define("Document Report").add("html", fileLink).map());
  }

  public void calculateRemainingDays(ActionRequest request, ActionResponse response)
      throws AxelorException {
    EmployeeFile empFile = request.getContext().asType(EmployeeFile.class);

    LocalDate expDate = empFile.getExpirationDate();
    if (expDate == null) {
      response.setValue("remainingDays", 0);
      return;
    }
    LocalDate todayDate = java.time.LocalDate.now();
    long result = ChronoUnit.DAYS.between(todayDate, expDate);
    response.setValue("remainingDays", result);
  }
}

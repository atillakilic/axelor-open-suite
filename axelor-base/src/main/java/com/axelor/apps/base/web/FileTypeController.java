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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.EmployeeFilePriseList;
import com.axelor.apps.base.db.FileType;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FileTypeController {

  public void setDocumentPrice(ActionRequest request, ActionResponse response) {
    FileType fileType = request.getContext().asType(FileType.class);
    List<EmployeeFilePriseList> filePriceList = fileType.getEmpFilePriseList();

    if (filePriceList.size() == 0) {
      return;
    }

    EmployeeFilePriseList maxFilePrise = filePriceList.get(0);
    LocalDate maxDate = maxFilePrise.getUpdatedDate();
    BigDecimal letestPrice = maxFilePrise.getPrice();

    for (int i = 1; i < filePriceList.size(); i++) {
      EmployeeFilePriseList filePrise = filePriceList.get(i);
      LocalDate nextDate = filePrise.getUpdatedDate();
      BigDecimal currrentFilePrice = filePrise.getPrice();
      if (nextDate.isAfter(maxDate)) {
        maxDate = nextDate;
        letestPrice = currrentFilePrice;
      }
    }
    response.setValue("price", letestPrice);
  }
}

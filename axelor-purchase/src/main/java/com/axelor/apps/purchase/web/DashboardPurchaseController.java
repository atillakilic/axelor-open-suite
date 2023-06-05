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
package com.axelor.apps.purchase.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.project.db.ProjectRu;
import com.axelor.apps.purchase.db.DashboardProductOverviewRu;
import com.axelor.apps.purchase.db.DashboardPurchaseRu;
import com.axelor.apps.purchase.db.DashboardSupplierBalanceRu;
import com.axelor.apps.purchase.db.DashboardSupplierCostRu;
import com.axelor.apps.purchase.db.PurchasePaymentRu;
import com.axelor.apps.purchase.db.PurchaseRequestLineRu;
import com.axelor.apps.purchase.db.PurchaseRequestRu;
import com.axelor.apps.purchase.db.RequestSupplierListRu;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRuRepository;
import com.axelor.apps.purchase.db.repo.RequestSupplierListRuRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class DashboardPurchaseController {

	public void onLoad(ActionRequest request, ActionResponse response) throws AxelorException {
		LocalDate today = LocalDate.now();
		LocalDate yesterDay = today.minusDays(1);
		response.setValue("productOverviewFromDate", yesterDay);
		response.setValue("productOverviewToDate", yesterDay);
		response.setValue("supplierCostFromDate", yesterDay);
		response.setValue("supplierCostToDate", yesterDay);
	}

	public void productOverview(ActionRequest request, ActionResponse response) throws AxelorException {
		DashboardPurchaseRu dashboard = request.getContext().asType(DashboardPurchaseRu.class);

		LocalDate fromDate = dashboard.getProductOverviewFromDate();
		LocalDate toDate = dashboard.getProductOverviewToDate();

		if (toDate == null || fromDate == null) {
			return;
		}

		List<PurchaseRequestRu> PurchaseRequestRuLine = Beans.get(PurchaseRequestRuRepository.class).all()
				.filter("self.requestDate >= ? AND self.requestDate <= ?", fromDate, toDate).fetch();

		List<DashboardProductOverviewRu> ProductOverviewList = new ArrayList<DashboardProductOverviewRu>();

		for (PurchaseRequestRu purchaseRequestRu : PurchaseRequestRuLine) {

			ProjectRu project = purchaseRequestRu.getProjectRu();
			LocalDate requestDate = purchaseRequestRu.getRequestDate();
			for (PurchaseRequestLineRu purchaseRequestLineRu : purchaseRequestRu.getPurchaseRequestLineList()) {
				DashboardProductOverviewRu dashboardProductOverviewRu = new DashboardProductOverviewRu();
				dashboardProductOverviewRu.setProject(project);
				dashboardProductOverviewRu.setProduct(purchaseRequestLineRu.getProduct());
				dashboardProductOverviewRu.setRequestDate(requestDate);
				dashboardProductOverviewRu.setQuantity(purchaseRequestLineRu.getQuantity());
				dashboardProductOverviewRu.setUnit(purchaseRequestLineRu.getUnit());

				ProductOverviewList.add(dashboardProductOverviewRu);
			}
		}
		response.setValue("productOverview", ProductOverviewList);
	}

	public void supplierBalance(ActionRequest request, ActionResponse response) throws AxelorException {
		DashboardPurchaseRu dashboard = request.getContext().asType(DashboardPurchaseRu.class);

		List<RequestSupplierListRu> supplierLineList = Beans.get(RequestSupplierListRuRepository.class).all().fetch();

		List<DashboardSupplierBalanceRu> dashboardSupplierBalanceRu = new ArrayList<DashboardSupplierBalanceRu>();

		for (RequestSupplierListRu supplierLine : supplierLineList) {
			boolean flagNewSupplier = true;
			Partner supplier = supplierLine.getSupplierUser();
			if (!supplierLine.getOfferSupplier()) {
				continue;
			}
			for (DashboardSupplierBalanceRu employeeDashboardEmpPenalty : dashboardSupplierBalanceRu) {
				Partner supplierAlredyfound = employeeDashboardEmpPenalty.getSupplierUser();
				if (supplier.equals(supplierAlredyfound)) {
					employeeDashboardEmpPenalty.setTotalBalance(
							employeeDashboardEmpPenalty.getTotalBalance().add(supplierLine.getTotalCost()));
					flagNewSupplier = false;
				}
			}
			if (flagNewSupplier) {
				DashboardSupplierBalanceRu dashboardSupplier = new DashboardSupplierBalanceRu();
				dashboardSupplier.setSupplierUser(supplier);
				dashboardSupplier.setTotalBalance(supplierLine.getTotalCost());

				dashboardSupplierBalanceRu.add(dashboardSupplier);
			}
		}
		response.setValue("supplierBalance", dashboardSupplierBalanceRu);
	}

	public void supplierCost(ActionRequest request, ActionResponse response) throws AxelorException {
		DashboardPurchaseRu dashboard = request.getContext().asType(DashboardPurchaseRu.class);

		System.err.println("sdskbx");
		
		LocalDate fromDate = dashboard.getSupplierCostFromDate();
		LocalDate toDate = dashboard.getSupplierCostToDate();

		if (toDate == null || fromDate == null) {
			return;
		}

		List<PurchaseRequestRu> purchaseRequestRuList = Beans.get(PurchaseRequestRuRepository.class).all()
				.filter("self.requestDate >= ? AND self.requestDate <= ?", fromDate, toDate).fetch();

		List<DashboardSupplierCostRu> dashboardSupplierCostRuList = new ArrayList<DashboardSupplierCostRu>();

		for (PurchaseRequestRu purchaseRequestRu : purchaseRequestRuList) {
			System.err.println(purchaseRequestRuList.size());

			ProjectRu project = purchaseRequestRu.getProjectRu();

			for (PurchaseRequestLineRu purchaseRequestLineRu : purchaseRequestRu.getPurchaseRequestLineList()) {

				PurchasePaymentRu PurchasePaymentRu = purchaseRequestLineRu.getPaymentInfo();

				for (RequestSupplierListRu requestSupplierListRu : purchaseRequestLineRu.getSupplierLineList()) {
					Partner supplier = requestSupplierListRu.getSupplierUser();
					if(supplier == null) {
						continue;
					}
					boolean flagNewSupplier = true;

					for (DashboardSupplierCostRu dashboardSupplierCostRu : dashboardSupplierCostRuList) {
						Partner supplierAlredyfound = dashboardSupplierCostRu.getSupplierUser();
						if (supplier.equals(supplierAlredyfound)) {
							
							if (PurchasePaymentRu == null) {
								dashboardSupplierCostRu.setNotPaid(dashboardSupplierCostRu.getNotPaid().add(requestSupplierListRu.getTotalCost()));
								dashboardSupplierCostRu.setTotalCost(dashboardSupplierCostRu.getTotalCost().add(requestSupplierListRu.getTotalCost()));
							} else {
								if (PurchasePaymentRu.getPaymentMethod() == 1) {
									dashboardSupplierCostRu.setPaidBank(dashboardSupplierCostRu.getPaidBank().add(requestSupplierListRu.getTotalCost()));
									dashboardSupplierCostRu.setTotalCost(dashboardSupplierCostRu.getTotalCost().add(requestSupplierListRu.getTotalCost()));
								}
								if (PurchasePaymentRu.getPaymentMethod() == 2) {
									dashboardSupplierCostRu.setPaidCash(dashboardSupplierCostRu.getPaidCash().add(requestSupplierListRu.getTotalCost()));
									dashboardSupplierCostRu.setTotalCost(dashboardSupplierCostRu.getTotalCost().add(requestSupplierListRu.getTotalCost()));
								}
							}
							
							flagNewSupplier = false;
						}
					}

					if (flagNewSupplier) {
						DashboardSupplierCostRu dashboardSupplierCostRu = new DashboardSupplierCostRu();
						dashboardSupplierCostRu.setSupplierUser(requestSupplierListRu.getSupplierUser());
						dashboardSupplierCostRu.setProject(project);

						if (PurchasePaymentRu == null) {
							dashboardSupplierCostRu.setPaidBank(new BigDecimal(0));
							dashboardSupplierCostRu.setPaidCash(new BigDecimal(0));
							dashboardSupplierCostRu.setNotPaid(requestSupplierListRu.getTotalCost());
							dashboardSupplierCostRu.setTotalCost(requestSupplierListRu.getTotalCost());
						} else {
							if (PurchasePaymentRu.getPaymentMethod() == 1) {
								dashboardSupplierCostRu.setPaidBank(requestSupplierListRu.getTotalCost());
								dashboardSupplierCostRu.setPaidCash(new BigDecimal(0));
								dashboardSupplierCostRu.setNotPaid(new BigDecimal(0));
								dashboardSupplierCostRu.setTotalCost(requestSupplierListRu.getTotalCost());
							}
							if (PurchasePaymentRu.getPaymentMethod() == 2) {
								dashboardSupplierCostRu.setPaidBank(new BigDecimal(0));
								dashboardSupplierCostRu.setPaidCash(requestSupplierListRu.getTotalCost());
								dashboardSupplierCostRu.setNotPaid(new BigDecimal(0));
								dashboardSupplierCostRu.setTotalCost(requestSupplierListRu.getTotalCost());
							}
						}
						
						dashboardSupplierCostRuList.add(dashboardSupplierCostRu);
					}
				}

			}
		}
		 response.setValue("supplierCost", dashboardSupplierCostRuList);
	}
}

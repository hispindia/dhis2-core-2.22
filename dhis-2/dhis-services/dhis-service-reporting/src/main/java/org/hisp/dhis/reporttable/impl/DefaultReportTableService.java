package org.hisp.dhis.reporttable.impl;

/*
 * Copyright (c) 2004-2016, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.analytics.AnalyticsService;
import org.hisp.dhis.common.AnalyticalObjectStore;
import org.hisp.dhis.common.DisplayProperty;
import org.hisp.dhis.common.GenericAnalyticalObjectService;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.report.ReportService;
import org.hisp.dhis.reporttable.ReportTable;
import org.hisp.dhis.reporttable.ReportTableService;
import org.hisp.dhis.system.grid.ListGrid;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lars Helge Overland
 */
@Transactional
public class DefaultReportTableService
    extends GenericAnalyticalObjectService<ReportTable>
    implements ReportTableService
{
    // ---------------------------------------------------------------------
    // Dependencies
    // ---------------------------------------------------------------------

    private AnalyticsService analyticsService;

    public void setAnalyticsService( AnalyticsService analyticsService )
    {
        this.analyticsService = analyticsService;
    }

    private AnalyticalObjectStore<ReportTable> reportTableStore;

    public void setReportTableStore( AnalyticalObjectStore<ReportTable> reportTableStore )
    {
        this.reportTableStore = reportTableStore;
    }

    private ReportService reportService;

    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    // -------------------------------------------------------------------------
    // ReportTableService implementation
    // -------------------------------------------------------------------------

    @Override
    protected AnalyticalObjectStore<ReportTable> getAnalyticalObjectStore()
    {
        return reportTableStore;
    }    
    
    @Override
    public Grid getReportTableGrid( String uid, I18nFormat format, Date reportingPeriod, String organisationUnitUid )
    {
        ReportTable reportTable = getReportTable( uid );
                
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitUid );

        List<OrganisationUnit> atLevels = new ArrayList<>();
        List<OrganisationUnit> inGroups = new ArrayList<>();
        
        if ( reportTable.hasOrganisationUnitLevels() )
        {
            atLevels.addAll( organisationUnitService.getOrganisationUnitsAtLevels( reportTable.getOrganisationUnitLevels(), reportTable.getOrganisationUnits() ) );
        }
        
        if ( reportTable.hasItemOrganisationUnitGroups() )
        {
            inGroups.addAll( organisationUnitService.getOrganisationUnits( reportTable.getItemOrganisationUnitGroups(), reportTable.getOrganisationUnits() ) );
        }
        
        reportTable.init( currentUserService.getCurrentUser(), reportingPeriod, organisationUnit, atLevels, inGroups, format );

        Map<String, Object> valueMap = analyticsService.getAggregatedDataValueMapping( reportTable, format );

        return reportTable.getGrid( new ListGrid(), valueMap, DisplayProperty.SHORTNAME, true );
    }

    @Override
    public ReportTable getReportTable( String uid, String mode )
    {
        if ( mode.equals( MODE_REPORT_TABLE ) )
        {
            return getReportTable( uid );
        }
        else if ( mode.equals( MODE_REPORT ) )
        {
            return reportService.getReport( uid ).getReportTable();
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    @Override
    public int saveReportTable( ReportTable reportTable )
    {
        return reportTableStore.save( reportTable );
    }

    @Override
    public void updateReportTable( ReportTable reportTable )
    {
        reportTableStore.update( reportTable );
    }

    @Override
    public void deleteReportTable( ReportTable reportTable )
    {
        reportTableStore.delete( reportTable );
    }

    @Override
    public ReportTable getReportTable( int id )
    {
        return reportTableStore.get( id );
    }

    @Override
    public ReportTable getReportTable( String uid )
    {
        return reportTableStore.getByUid( uid );
    }

    @Override
    public ReportTable getReportTableNoAcl( String uid )
    {
        return reportTableStore.getByUidNoAcl( uid );
    }

    @Override
    public List<ReportTable> getReportTablesByUid( List<String> uids )
    {
        return reportTableStore.getByUid( uids );
    }

    @Override
    public List<ReportTable> getAllReportTables()
    {
        return reportTableStore.getAll();
    }

    @Override
    public List<ReportTable> getReportTableByName( String name )
    {
        return reportTableStore.getAllEqName( name );
    }

    @Override
    public List<ReportTable> getReportTablesBetweenByName( String name, int first, int max )
    {
        return reportTableStore.getAllLikeName( name, first, max );
    }

    @Override
    public int getReportTableCount()
    {
        return reportTableStore.getCount();
    }

    @Override
    public int getReportTableCountByName( String name )
    {
        return reportTableStore.getCountLikeName( name );
    }

    @Override
    public List<ReportTable> getReportTablesBetween( int first, int max )
    {
        return reportTableStore.getAllOrderedName( first, max );
    }
}

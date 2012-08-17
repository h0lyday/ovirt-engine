package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class ClusterPolicyModel extends EntityModel
{
    public static Integer lowLimitPowerSaving = null;
    public static Integer highLimitPowerSaving = null;
    public static Integer highLimitEvenlyDistributed = null;

    private Integer noOfVolumesTotal;
    private Integer noOfVolumesUp;
    private Integer noOfVolumesDown;

    public String getNoOfVolumesTotal() {
        return Integer.toString(noOfVolumesTotal);
    }

    public void setNoOfVolumesTotal(Integer noOfVolumesTotal) {
        this.noOfVolumesTotal = noOfVolumesTotal;
    }

    public String getNoOfVolumesUp() {
        return Integer.toString(noOfVolumesUp);
    }

    public void setNoOfVolumesUp(Integer noOfVolumesUp) {
        this.noOfVolumesUp = noOfVolumesUp;
    }

    public String getNoOfVolumesDown() {
        return Integer.toString(noOfVolumesDown);
    }

    public void setNoOfVolumesDown(Integer noOfVolumesDown) {
        this.noOfVolumesDown = noOfVolumesDown;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    @Override
    public VDSGroup getEntity()
    {
        return (VDSGroup) ((super.getEntity() instanceof VDSGroup) ? super.getEntity() : null);
    }

    public void setEntity(VDSGroup value)
    {
        super.setEntity(value);
    }

    private EntityModel privateOverCommitTime;

    public EntityModel getOverCommitTime()
    {
        return privateOverCommitTime;
    }

    public void setOverCommitTime(EntityModel value)
    {
        privateOverCommitTime = value;
    }

    private boolean hasOverCommitLowLevel;

    public boolean getHasOverCommitLowLevel()
    {
        return hasOverCommitLowLevel;
    }

    public void setHasOverCommitLowLevel(boolean value)
    {
        if (hasOverCommitLowLevel != value)
        {
            hasOverCommitLowLevel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasOverCommitLowLevel")); //$NON-NLS-1$
        }
    }

    public boolean hasOverCommitHighLevel;

    public boolean getHasOverCommitHighLevel()
    {
        return hasOverCommitHighLevel;
    }

    public void setHasOverCommitHighLevel(boolean value)
    {
        if (hasOverCommitHighLevel != value)
        {
            hasOverCommitHighLevel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasOverCommitHighLevel")); //$NON-NLS-1$
        }
    }

    // Editing features
    private VdsSelectionAlgorithm selectionAlgorithm = VdsSelectionAlgorithm.values()[0];

    public VdsSelectionAlgorithm getSelectionAlgorithm()
    {
        return selectionAlgorithm;
    }

    public void setSelectionAlgorithm(VdsSelectionAlgorithm value)
    {
        if (selectionAlgorithm != value)
        {
            selectionAlgorithm = value;
            SelectionAlgorithmChanged();
            OnPropertyChanged(new PropertyChangedEventArgs("SelectionAlgorithm")); //$NON-NLS-1$
        }
    }

    private int overCommitLowLevel;

    public int getOverCommitLowLevel()
    {
        return overCommitLowLevel;
    }

    public void setOverCommitLowLevel(int value)
    {
        if (overCommitLowLevel != value)
        {
            overCommitLowLevel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("OverCommitLowLevel")); //$NON-NLS-1$
        }
    }

    private int overCommitHighLevel;

    public int getOverCommitHighLevel()
    {
        return overCommitHighLevel;
    }

    public void setOverCommitHighLevel(int value)
    {
        if (overCommitHighLevel != value)
        {
            overCommitHighLevel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("OverCommitHighLevel")); //$NON-NLS-1$
        }
    }

    public void SaveDefaultValues()
    {
        if (getSelectionAlgorithm() == VdsSelectionAlgorithm.EvenlyDistribute)
        {
            highLimitEvenlyDistributed = getOverCommitHighLevel();
        }
        else if (getSelectionAlgorithm() == VdsSelectionAlgorithm.PowerSave)
        {
            lowLimitPowerSaving = getOverCommitLowLevel();
            highLimitPowerSaving = getOverCommitHighLevel();
        }
    }

    public ClusterPolicyModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$

        setNoOfVolumesTotal(0);
        setNoOfVolumesUp(0);
        setNoOfVolumesDown(0);

        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setOverCommitTime(new EntityModel());

        // Set all properties according to default selected algorithm:
        SelectionAlgorithmChanged();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterPolicyModel.highLimitEvenlyDistributed = (Integer) result;
            }
        };
        if (ClusterPolicyModel.highLimitEvenlyDistributed == null)
        {
            AsyncDataProvider.GetHighUtilizationForEvenDistribution(_asyncQuery);
        }
        _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterPolicyModel.lowLimitPowerSaving = (Integer) result;
            }
        };
        if (ClusterPolicyModel.lowLimitPowerSaving == null)
        {
            AsyncDataProvider.GetLowUtilizationForPowerSave(_asyncQuery);
        }

        _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterPolicyModel.highLimitPowerSaving = (Integer) result;
            }
        };
        if (highLimitPowerSaving == null)
        {
            AsyncDataProvider.GetHighUtilizationForPowerSave(_asyncQuery);
        }
    }

    public void Edit()
    {
        if (getWindow() != null)
        {
            return;
        }

        ClusterPolicyModel model = new ClusterPolicyModel();

        model.setTitle(ConstantsManager.getInstance().getConstants().editPolicyTitle());
        model.setHashName("edit_policy"); //$NON-NLS-1$

        model.setSelectionAlgorithm(getEntity().getselection_algorithm());
        model.getOverCommitTime().setEntity(getEntity().getcpu_over_commit_duration_minutes());
        model.setOverCommitLowLevel(getEntity().getlow_utilization());
        model.setOverCommitHighLevel(getEntity().gethigh_utilization());

        model.SaveDefaultValues();

        setWindow(model);

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnSave()
    {
        ClusterPolicyModel model = (ClusterPolicyModel) getWindow();

        if (getEntity() == null)
        {
            Cancel();
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        VDSGroup cluster = (VDSGroup) Cloner.clone(getEntity());
        cluster.setselection_algorithm(model.getSelectionAlgorithm());
        if (model.getOverCommitTime().getIsAvailable())
        {
            cluster.setcpu_over_commit_duration_minutes(Integer.parseInt(model.getOverCommitTime()
                    .getEntity()
                    .toString()));
        }
        cluster.setlow_utilization(model.getOverCommitLowLevel());
        cluster.sethigh_utilization(model.getOverCommitHighLevel());

        Frontend.RunAction(VdcActionType.UpdateVdsGroup, new VdsGroupOperationParameters(cluster));

        Cancel();
    }

    public boolean Validate()
    {
        IntegerValidation tempVar = new IntegerValidation();
        tempVar.setMinimum(1);
        tempVar.setMaximum(100);
        getOverCommitTime().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

        return getOverCommitTime().getIsValid();
    }

    public void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            UpdateProperties();
            UpdateVolumeDetails();
        }

        UpdateActionAvailability();
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("selection_algorithm")) //$NON-NLS-1$
        {
            UpdateProperties();
        }
    }

    private void UpdateActionAvailability()
    {
        getEditCommand().setIsExecutionAllowed(getEntity() != null);
    }

    private void UpdateProperties()
    {
        getOverCommitTime().setIsAvailable(getEntity().getselection_algorithm() != VdsSelectionAlgorithm.None);
        getOverCommitTime().setIsChangable(getOverCommitTime().getIsAvailable());
        setHasOverCommitLowLevel(getEntity().getselection_algorithm() == VdsSelectionAlgorithm.PowerSave);
        setHasOverCommitHighLevel(getEntity().getselection_algorithm() != VdsSelectionAlgorithm.None);
    }

    private void UpdateVolumeDetails()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterPolicyModel innerGeneralModel = (ClusterPolicyModel) model;
                ArrayList<GlusterVolumeEntity> volumeList = (ArrayList<GlusterVolumeEntity>) result;
                int volumesUp = 0;
                int volumesDown = 0;
                for (GlusterVolumeEntity volumeEntity : volumeList)
                {
                    if (volumeEntity.getStatus() == GlusterStatus.UP)
                    {
                        volumesUp++;
                    }
                    else
                    {
                        volumesDown++;
                    }
                }
                setNoOfVolumesTotal(volumeList.size());
                setNoOfVolumesUp(volumesUp);
                setNoOfVolumesDown(volumesDown);
            }
        };
        AsyncDataProvider.GetVolumeList(_asyncQuery, getEntity().getname());
    }

    private void SelectionAlgorithmChanged()
    {
        setHasOverCommitLowLevel(getSelectionAlgorithm() != VdsSelectionAlgorithm.EvenlyDistribute);

        switch (getSelectionAlgorithm())
        {
        case None:
            getOverCommitTime().setIsAvailable(false);
            setHasOverCommitLowLevel(false);
            setHasOverCommitHighLevel(false);
            setOverCommitLowLevel(0);
            setOverCommitHighLevel(0);
            break;

        case EvenlyDistribute:
            getOverCommitTime().setIsAvailable(true);
            getOverCommitTime().setIsChangable(true);
            setHasOverCommitLowLevel(false);
            setHasOverCommitHighLevel(true);
            setOverCommitLowLevel(0);
            setOverCommitHighLevel((highLimitEvenlyDistributed == null ? 0 : highLimitEvenlyDistributed));
            break;

        case PowerSave:
            getOverCommitTime().setIsAvailable(true);
            getOverCommitTime().setIsChangable(true);
            setHasOverCommitLowLevel(true);
            setHasOverCommitHighLevel(true);
            setOverCommitLowLevel((ClusterPolicyModel.lowLimitPowerSaving == null ? 0
                    : ClusterPolicyModel.lowLimitPowerSaving));

            setOverCommitHighLevel((ClusterPolicyModel.highLimitPowerSaving == null ? 0
                    : ClusterPolicyModel.highLimitPowerSaving));
            break;
        }
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getEditCommand())
        {
            Edit();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }
}

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.Console;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VirtIOSCSI;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.api.resource.GraphicsConsolesResource;
import org.ovirt.engine.api.resource.ReadOnlyDevicesResource;
import org.ovirt.engine.api.resource.TemplateDisksResource;
import org.ovirt.engine.api.resource.TemplateResource;
import org.ovirt.engine.api.resource.WatchdogsResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.IconHelper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateResource
    extends AbstractBackendActionableResource<Template, VmTemplate>
    implements TemplateResource {

    static final String[] SUB_COLLECTIONS = {"disks", "nics", "cdroms", "tags", "permissions", "watchdogs", "graphicsconsoles"};

    public BackendTemplateResource(String id) {
        super(id, Template.class, VmTemplate.class, SUB_COLLECTIONS);
    }

    @Override
    public Template get() {
        Template template = performGet(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(guid));
        if (template != null) {
            DisplayHelper.adjustDisplayData(this, template);
        }
        return template;
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveVmTemplate, new VmTemplateParametersBase(guid));
    }

    @Override
    public Template update(Template incoming) {
        validateEnums(Template.class, incoming);
        validateIconParams(incoming);
        Template result = performUpdate(
            incoming,
            new QueryIdResolver<Guid>(VdcQueryType.GetVmTemplate, GetVmTemplateParameters.class),
            VdcActionType.UpdateVmTemplate,
            new UpdateParametersProvider()
        );
        if (result != null) {
            DisplayHelper.adjustDisplayData(this, result);
        }
        return result;
    }

    private void validateIconParams(Template incoming) {
        if (!IconHelper.validateIconParameters(incoming)) {
            throw new BaseBackendResource.WebFaultException(null,
                    localize(Messages.INVALID_ICON_PARAMETERS),
                    Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public Response export(Action action) {
        validateParameters(action, "storageDomain.id|name");

        MoveVmParameters params = new MoveVmParameters(guid, getStorageDomainId(action));

        if (action.isSetExclusive() && action.isExclusive()) {
            params.setForceOverride(true);
        }

        return doAction(VdcActionType.ExportVmTemplate, params, action, PollingType.JOB);
    }

    @Override
    public ReadOnlyDevicesResource<CdRom, CdRoms> getCdRomsResource() {
        return inject(new BackendReadOnlyCdRomsResource<VmTemplate>
                (VmTemplate.class,
                        guid,
                        VdcQueryType.GetVmTemplate,
                        new GetVmTemplateParameters(guid)));
    }

    @Override
    public TemplateDisksResource getDisksResource() {
        return inject(new BackendTemplateDisksResource(guid,
                VdcQueryType.GetVmTemplatesDisks,
                new IdQueryParameters(guid)));
    }

    @Override
    public DevicesResource<NIC, Nics> getNicsResource() {
        return inject(new BackendTemplateNicsResource(guid));
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        return inject(new BackendTemplateTagsResource(id));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                VdcQueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(guid),
                Template.class,
                VdcObjectType.VmTemplate));
    }

    @Override
    public CreationResource getCreationSubresource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public ActionResource getActionSubresource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    protected class UpdateParametersProvider implements ParametersProvider<Template, VmTemplate> {
        @Override
        public VdcActionParametersBase getParameters(Template incoming, VmTemplate entity) {
            VmTemplate updated = getMapper(modelType, VmTemplate.class).map(incoming, entity);
            updated.setUsbPolicy(VmMapper.getUsbPolicyOnUpdate(incoming.getUsb(), entity.getUsbPolicy()));
            UpdateVmTemplateParameters params = new UpdateVmTemplateParameters(updated);
            if (incoming.isSetRngDevice()) {
                params.setUpdateRngDevice(true);
                params.setRngDevice(RngDeviceMapper.map(incoming.getRngDevice(), null));
            }
            if(incoming.isSetSoundcardEnabled()) {
                params.setSoundDeviceEnabled(incoming.isSoundcardEnabled());
            }
            if (incoming.isSetVirtioScsi()) {
                if (incoming.getVirtioScsi().isSetEnabled()) {
                    params.setVirtioScsiEnabled(incoming.getVirtioScsi().isEnabled());
                }
            }

            IconHelper.setIconToParams(incoming, params);
            DisplayHelper.setGraphicsToParams(incoming.getDisplay(), params);

            if (incoming.isSetMemoryPolicy() && incoming.getMemoryPolicy().isSetBallooning()) {
                params.setBalloonEnabled(incoming.getMemoryPolicy().isBallooning());
            }

            return getMapper(modelType, UpdateVmTemplateParameters.class).map(incoming, params);
        }
    }

    private VDSGroup lookupCluster(Guid id) {
        return getEntity(VDSGroup.class, VdcQueryType.GetVdsGroupByVdsGroupId, new IdQueryParameters(id), "GetVdsGroupByVdsGroupId");
    }

    @Override
    protected Template doPopulate(Template model, VmTemplate entity) {
        if (!model.isSetConsole()) {
            model.setConsole(new Console());
        }
        model.getConsole().setEnabled(!getConsoleDevicesForEntity(entity.getId()).isEmpty());
        if (!model.isSetVirtioScsi()) {
            model.setVirtioScsi(new VirtIOSCSI());
        }
        model.getVirtioScsi().setEnabled(!VmHelper.getVirtioScsiControllersForEntity(this, entity.getId()).isEmpty());
        model.setSoundcardEnabled(VmHelper.getSoundDevicesForEntity(this, entity.getId()).isEmpty());
        setRngDevice(model);
        return model;
    }

    @Override
    protected Template deprecatedPopulate(Template model, VmTemplate entity) {
        MemoryPolicyHelper.setupMemoryBalloon(model, this);
        return model;
    }

    private void setRngDevice(Template model) {
        List<VmRngDevice> rngDevices = getEntity(List.class,
                VdcQueryType.GetRngDevice,
                new IdQueryParameters(Guid.createGuidFromString(model.getId())),
                "GetRngDevice", true);

        if (rngDevices != null && !rngDevices.isEmpty()) {
            model.setRngDevice(RngDeviceMapper.map(rngDevices.get(0), null));
        }
    }

    @Override
    public WatchdogsResource getWatchdogsResource() {
        return inject(new BackendTemplateWatchdogsResource(guid,
                VdcQueryType.GetWatchdog,
                new IdQueryParameters(guid)));
    }

    @Override
    public GraphicsConsolesResource getGraphicsConsolesResource() {
        return inject(new BackendTemplateGraphicsConsolesResource(guid));
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                VdcQueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

}


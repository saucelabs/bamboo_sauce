[#assign createMode=req.servletPath.contains('/build/admin/create/') /]
<script type="text/javascript">
    [#if createMode]
    SAUCE.builderModePrefix = "createBuildBuilder";
        [#else]
        SAUCE.builderModePrefix = "updateBuildBuilder";
    [/#if]
    jQuery(document).ready(function()
    {
    });

</script>
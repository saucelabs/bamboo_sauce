if (typeof SAUCE == "undefined")
{
    var SAUCE = new Object();
    SAUCE.builderModePrefix = "";
    SAUCE.postModePrefix = "";
}

SAUCE.doExtensionSave = function()
{
    var versionTypeKey = jQuery("#brmp_versionTypeKey").val();
    var versionTypeDescription = jQuery("#brmp_versionTypeDescription").val();
    var versionTypeCaseSensitive = jQuery("#brmp_versionTypeCaseSensitive").is(':checked');

    if (versionTypeCaseSensitive)
    {
        var caseImage = '<img src="' + BAMBOO.contextPath + '/images/jt/icn_permission_check.gif">';
    } else
    {
        var caseImage = '<img src="' + BAMBOO.contextPath + '/images/jt/icn_permission_unchecked.gif">';
    }

    var curJSON = jQuery("#" + SAUCE.builderModePrefix + "_custom_brmp_versiontypes_json").val();
    if (curJSON == "" || curJSON == null || curJSON == undefined || curJSON.substr(0, 1) != "[")
    {
        curJSON = "[]";
    }
    var jsonArray = jQuery.evalJSON(curJSON);

    var newEntry = new Object();
    newEntry.key = versionTypeKey;
    newEntry.description = versionTypeDescription;
    newEntry.caseSensitive = versionTypeCaseSensitive;
    newEntry.tagReleased = false;
    newEntry.tagUnreleased = false;

    jsonArray[jsonArray.length] = newEntry;

    var newJSON = jQuery.toJSON(jsonArray);
    jQuery("#" + SAUCE.builderModePrefix + "_custom_brmp_versiontypes_json").val(newJSON);

    jQuery("#brmpAddVersionTypeFormRow").before('<tr><td>' + versionTypeKey + '</td> <td>' + versionTypeDescription + '</td><td class="checkboxCell">' + caseImage + '</td><td class="checkboxCell"><input class="brmpRemoveVersionTypeButton" type="image" src="' + BAMBOO.contextPath + '/images/icons/trash_16.gif" alt="Remove Version Type from list" title="Remove Version Type from list"></td></tr>');
    jQuery(".brmpRemoveVersionTypeButton").unbind('click');
    jQuery(".brmpRemoveVersionTypeButton").click(SAUCE.doVersionTypeDelete);

    jQuery("#brmp_versionTypeKey").val("");
    jQuery("#brmp_versionTypeDescription").val("");
    jQuery("#brmp_versionTypeCaseSensitive").attr('checked', false);
    return false;
}

SAUCE.doExtensionDelete = function()
{
    var rowIndex = jQuery(this).parent().parent()[0].sectionRowIndex;
    var row = jQuery(this).parent().parent();
    var removeIndex = rowIndex - 2;

    var curJSON = jQuery("#" + SAUCE.builderModePrefix + "_custom_brmp_versiontypes_json").val();
    var jsonArray = jQuery.evalJSON(curJSON);
    jsonArray.splice(removeIndex, 1);

    var newJSON = jQuery.toJSON(jsonArray);
    jQuery("#" + SAUCE.builderModePrefix + "_custom_brmp_versiontypes_json").val(newJSON);

    row.remove();

    return false;
}

jQuery(document).ready(function()
{
    jQuery("#saveExtensionButton").click(SAUCE.doExtensionSave);

    jQuery(".sauceRemoveExtensionButton").click(SAUCE.doExtensionDelete);

    //setup tab switching
    var builderItems = jQuery('#sauceBuilderTabs>ul>li');
    builderItems.click(function()
    {
        builderItems.removeClass('selected');
        jQuery(this).addClass('selected');

        var index = builderItems.index(jQuery(this));
        jQuery('#sauceBuilderTabs>div').hide().eq(index).show();
    }).eq(0).click();

    jQuery("#webDriverBrowsers, #appiumBrowser").chosen({disable_search_threshold: 10});
});



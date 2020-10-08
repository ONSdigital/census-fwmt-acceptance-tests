<#if surveyType == "CE">
    <#if hasUsualResidents>
        "usualResidents": "5"
<#--    <#elseif businessFunction != "New Unit Reported">-->
    <#else>
        "usualResidents": "1"
<#--    <#else>-->
<#--        "usualResidents": "0"-->
    </#if>

<#else>"ceDetails": {
    <#if hasUsualResidents || businessFunction == "Switch Feedback Site">
        "usualResidents": "5"
    <#else>
        "usualResidents": "1"
    </#if>
},
</#if>

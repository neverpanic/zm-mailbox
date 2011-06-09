/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2010-2011 Zimbra, Inc.
 *
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.3 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.dom4j.Document;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.soap.util.JaxbInfo;

public final class JaxbUtil {

    private static final Log LOG = ZimbraLog.soap;
    private static final Class<?>[] MESSAGE_CLASSES;
    private static final String ACCOUNT_JAXB_PACKAGE =
        "com.zimbra.soap.account.message";
    private static final String ADMIN_JAXB_PACKAGE =
        "com.zimbra.soap.admin.message";
    private static final String MAIL_JAXB_PACKAGE =
        "com.zimbra.soap.mail.message";
    private static JAXBContext JAXB_CONTEXT;

    static {
        MESSAGE_CLASSES = new Class<?>[] {
            // zimbraAccount
            com.zimbra.soap.account.message.AuthRequest.class,
            com.zimbra.soap.account.message.AuthResponse.class,
            com.zimbra.soap.account.message.ChangePasswordRequest.class,
            com.zimbra.soap.account.message.ChangePasswordResponse.class,
            com.zimbra.soap.account.message.CreateIdentityRequest.class,
            com.zimbra.soap.account.message.CreateIdentityResponse.class,
            com.zimbra.soap.account.message.CreateSignatureRequest.class,
            com.zimbra.soap.account.message.CreateSignatureResponse.class,
            com.zimbra.soap.account.message.DeleteIdentityRequest.class,
            com.zimbra.soap.account.message.DeleteIdentityResponse.class,
            com.zimbra.soap.account.message.DeleteSignatureRequest.class,
            com.zimbra.soap.account.message.DeleteSignatureResponse.class,
            com.zimbra.soap.account.message.EndSessionRequest.class,
            com.zimbra.soap.account.message.EndSessionResponse.class,
            com.zimbra.soap.account.message.GetAccountInfoRequest.class,
            com.zimbra.soap.account.message.GetAccountInfoResponse.class,
            com.zimbra.soap.account.message.GetAllLocalesRequest.class,
            com.zimbra.soap.account.message.GetAllLocalesResponse.class,
            com.zimbra.soap.account.message.GetAvailableCsvFormatsRequest.class,
            com.zimbra.soap.account.message.GetAvailableCsvFormatsResponse.class,
            com.zimbra.soap.account.message.GetAvailableLocalesRequest.class,
            com.zimbra.soap.account.message.GetAvailableLocalesResponse.class,
            com.zimbra.soap.account.message.GetAvailableSkinsRequest.class,
            com.zimbra.soap.account.message.GetAvailableSkinsResponse.class,
            com.zimbra.soap.account.message.GetDistributionListMembersRequest.class,
            com.zimbra.soap.account.message.GetDistributionListMembersResponse.class,
            com.zimbra.soap.account.message.GetIdentitiesRequest.class,
            com.zimbra.soap.account.message.GetIdentitiesResponse.class,
            com.zimbra.soap.account.message.GetInfoRequest.class,
            com.zimbra.soap.account.message.GetInfoResponse.class,
            com.zimbra.soap.account.message.GetPrefsRequest.class,
            com.zimbra.soap.account.message.GetPrefsResponse.class,
            com.zimbra.soap.account.message.GetShareInfoRequest.class,
            com.zimbra.soap.account.message.GetShareInfoResponse.class,
            com.zimbra.soap.account.message.GetSignaturesRequest.class,
            com.zimbra.soap.account.message.GetSignaturesResponse.class,
            com.zimbra.soap.account.message.GetVersionInfoRequest.class,
            com.zimbra.soap.account.message.GetVersionInfoResponse.class,
            com.zimbra.soap.account.message.GetWhiteBlackListRequest.class,
            com.zimbra.soap.account.message.GetWhiteBlackListResponse.class,
            com.zimbra.soap.account.message.ModifyIdentityRequest.class,
            com.zimbra.soap.account.message.ModifyIdentityResponse.class,
            com.zimbra.soap.account.message.ModifyPrefsRequest.class,
            com.zimbra.soap.account.message.ModifyPrefsResponse.class,
            com.zimbra.soap.account.message.ModifyPropertiesRequest.class,
            com.zimbra.soap.account.message.ModifyPropertiesResponse.class,
            com.zimbra.soap.account.message.ModifySignatureRequest.class,
            com.zimbra.soap.account.message.ModifySignatureResponse.class,
            com.zimbra.soap.account.message.ModifyWhiteBlackListRequest.class,
            com.zimbra.soap.account.message.ModifyWhiteBlackListResponse.class,

            // zimbraMail
            com.zimbra.soap.mail.message.AddAppointmentInviteRequest.class,
            com.zimbra.soap.mail.message.AddAppointmentInviteResponse.class,
            com.zimbra.soap.mail.message.AddCommentRequest.class,
            com.zimbra.soap.mail.message.AddCommentResponse.class,
            com.zimbra.soap.mail.message.AddTaskInviteRequest.class,
            com.zimbra.soap.mail.message.AddTaskInviteResponse.class,
            com.zimbra.soap.mail.message.AnnounceOrganizerChangeRequest.class,
            com.zimbra.soap.mail.message.AnnounceOrganizerChangeResponse.class,
            com.zimbra.soap.mail.message.ApplyFilterRulesRequest.class,
            com.zimbra.soap.mail.message.ApplyFilterRulesResponse.class,
            com.zimbra.soap.mail.message.ApplyOutgoingFilterRulesRequest.class,
            com.zimbra.soap.mail.message.ApplyOutgoingFilterRulesResponse.class,
            com.zimbra.soap.mail.message.AutoCompleteRequest.class,
            com.zimbra.soap.mail.message.AutoCompleteResponse.class,
            com.zimbra.soap.mail.message.BrowseRequest.class,
            com.zimbra.soap.mail.message.BrowseResponse.class,
            com.zimbra.soap.mail.message.CancelAppointmentRequest.class,
            com.zimbra.soap.mail.message.CancelAppointmentResponse.class,
            com.zimbra.soap.mail.message.CancelTaskRequest.class,
            com.zimbra.soap.mail.message.CancelTaskResponse.class,
            com.zimbra.soap.mail.message.CheckPermissionRequest.class,
            com.zimbra.soap.mail.message.CheckPermissionResponse.class,
            com.zimbra.soap.mail.message.CheckRecurConflictsRequest.class,
            com.zimbra.soap.mail.message.CheckRecurConflictsResponse.class,
            com.zimbra.soap.mail.message.CheckSpellingRequest.class,
            com.zimbra.soap.mail.message.CheckSpellingResponse.class,
            com.zimbra.soap.mail.message.CompleteTaskInstanceRequest.class,
            com.zimbra.soap.mail.message.CompleteTaskInstanceResponse.class,
            com.zimbra.soap.mail.message.ContactActionRequest.class,
            com.zimbra.soap.mail.message.ContactActionResponse.class,
            com.zimbra.soap.mail.message.ConvActionRequest.class,
            com.zimbra.soap.mail.message.ConvActionResponse.class,
            com.zimbra.soap.mail.message.CounterAppointmentRequest.class,
            com.zimbra.soap.mail.message.CounterAppointmentResponse.class,
            com.zimbra.soap.mail.message.CreateAppointmentExceptionRequest.class,
            com.zimbra.soap.mail.message.CreateAppointmentExceptionResponse.class,
            com.zimbra.soap.mail.message.CreateAppointmentRequest.class,
            com.zimbra.soap.mail.message.CreateAppointmentResponse.class,
            com.zimbra.soap.mail.message.CreateContactRequest.class,
            com.zimbra.soap.mail.message.CreateContactResponse.class,
            com.zimbra.soap.mail.message.CreateDataSourceRequest.class,
            com.zimbra.soap.mail.message.CreateDataSourceResponse.class,
            com.zimbra.soap.mail.message.CreateFolderRequest.class,
            com.zimbra.soap.mail.message.CreateFolderResponse.class,
            com.zimbra.soap.mail.message.CreateMountpointRequest.class,
            com.zimbra.soap.mail.message.CreateMountpointResponse.class,
            com.zimbra.soap.mail.message.CreateNoteRequest.class,
            com.zimbra.soap.mail.message.CreateNoteResponse.class,
            com.zimbra.soap.mail.message.CreateSearchFolderRequest.class,
            com.zimbra.soap.mail.message.CreateSearchFolderResponse.class,
            com.zimbra.soap.mail.message.CreateTagRequest.class,
            com.zimbra.soap.mail.message.CreateTagResponse.class,
            com.zimbra.soap.mail.message.CreateTaskExceptionRequest.class,
            com.zimbra.soap.mail.message.CreateTaskExceptionResponse.class,
            com.zimbra.soap.mail.message.CreateTaskRequest.class,
            com.zimbra.soap.mail.message.CreateTaskResponse.class,
            com.zimbra.soap.mail.message.CreateWaitSetRequest.class,
            com.zimbra.soap.mail.message.CreateWaitSetResponse.class,
            com.zimbra.soap.mail.message.DeclineCounterAppointmentRequest.class,
            com.zimbra.soap.mail.message.DeclineCounterAppointmentResponse.class,
            com.zimbra.soap.mail.message.DeleteDataSourceRequest.class,
            com.zimbra.soap.mail.message.DeleteDataSourceResponse.class,
            com.zimbra.soap.mail.message.DestroyWaitSetRequest.class,
            com.zimbra.soap.mail.message.DestroyWaitSetResponse.class,
            com.zimbra.soap.mail.message.DiffDocumentRequest.class,
            com.zimbra.soap.mail.message.DiffDocumentResponse.class,
            com.zimbra.soap.mail.message.DismissCalendarItemAlarmRequest.class,
            com.zimbra.soap.mail.message.DismissCalendarItemAlarmResponse.class,
            com.zimbra.soap.mail.message.EmptyDumpsterRequest.class,
            com.zimbra.soap.mail.message.EmptyDumpsterResponse.class,
            com.zimbra.soap.mail.message.EnableSharedReminderRequest.class,
            com.zimbra.soap.mail.message.EnableSharedReminderResponse.class,
            com.zimbra.soap.mail.message.ExpandRecurRequest.class,
            com.zimbra.soap.mail.message.ExpandRecurResponse.class,
            com.zimbra.soap.mail.message.ExportContactsRequest.class,
            com.zimbra.soap.mail.message.ExportContactsResponse.class,
            com.zimbra.soap.mail.message.FolderActionRequest.class,
            com.zimbra.soap.mail.message.FolderActionResponse.class,
            com.zimbra.soap.mail.message.ForwardAppointmentInviteRequest.class,
            com.zimbra.soap.mail.message.ForwardAppointmentInviteResponse.class,
            com.zimbra.soap.mail.message.ForwardAppointmentRequest.class,
            com.zimbra.soap.mail.message.ForwardAppointmentResponse.class,
            com.zimbra.soap.mail.message.GenerateUUIDRequest.class,
            com.zimbra.soap.mail.message.GenerateUUIDResponse.class,
            com.zimbra.soap.mail.message.GetAppointmentRequest.class,
            com.zimbra.soap.mail.message.GetAppointmentResponse.class,
            com.zimbra.soap.mail.message.GetApptSummariesRequest.class,
            com.zimbra.soap.mail.message.GetApptSummariesResponse.class,
            com.zimbra.soap.mail.message.GetCalendarItemSummariesRequest.class,
            com.zimbra.soap.mail.message.GetCalendarItemSummariesResponse.class,
            com.zimbra.soap.mail.message.GetCommentsRequest.class,
            com.zimbra.soap.mail.message.GetCommentsResponse.class,
            com.zimbra.soap.mail.message.GetContactsRequest.class,
            com.zimbra.soap.mail.message.GetContactsResponse.class,
            com.zimbra.soap.mail.message.GetConvRequest.class,
            com.zimbra.soap.mail.message.GetConvResponse.class,
            com.zimbra.soap.mail.message.GetCustomMetadataRequest.class,
            com.zimbra.soap.mail.message.GetCustomMetadataResponse.class,
            com.zimbra.soap.mail.message.GetDataSourcesRequest.class,
            com.zimbra.soap.mail.message.GetDataSourcesResponse.class,
            com.zimbra.soap.mail.message.GetEffectiveFolderPermsRequest.class,
            com.zimbra.soap.mail.message.GetEffectiveFolderPermsResponse.class,
            com.zimbra.soap.mail.message.GetFilterRulesRequest.class,
            com.zimbra.soap.mail.message.GetFilterRulesResponse.class,
            com.zimbra.soap.mail.message.GetFolderRequest.class,
            com.zimbra.soap.mail.message.GetFolderResponse.class,
            com.zimbra.soap.mail.message.GetFreeBusyRequest.class,
            com.zimbra.soap.mail.message.GetFreeBusyResponse.class,
            com.zimbra.soap.mail.message.GetICalRequest.class,
            com.zimbra.soap.mail.message.GetICalResponse.class,
            com.zimbra.soap.mail.message.GetImportStatusRequest.class,
            com.zimbra.soap.mail.message.GetImportStatusResponse.class,
            com.zimbra.soap.mail.message.GetItemRequest.class,
            com.zimbra.soap.mail.message.GetItemResponse.class,
            com.zimbra.soap.mail.message.GetMailboxMetadataRequest.class,
            com.zimbra.soap.mail.message.GetMailboxMetadataResponse.class,
            com.zimbra.soap.mail.message.GetMiniCalRequest.class,
            com.zimbra.soap.mail.message.GetMiniCalResponse.class,
            com.zimbra.soap.mail.message.GetMsgMetadataRequest.class,
            com.zimbra.soap.mail.message.GetMsgMetadataResponse.class,
            com.zimbra.soap.mail.message.GetNoteRequest.class,
            com.zimbra.soap.mail.message.GetNoteResponse.class,
            com.zimbra.soap.mail.message.GetOutgoingFilterRulesRequest.class,
            com.zimbra.soap.mail.message.GetOutgoingFilterRulesResponse.class,
            com.zimbra.soap.mail.message.GetPermissionRequest.class,
            com.zimbra.soap.mail.message.GetPermissionResponse.class,
            com.zimbra.soap.mail.message.GetRecurRequest.class,
            com.zimbra.soap.mail.message.GetRecurResponse.class,
            com.zimbra.soap.mail.message.GetRulesRequest.class,
            com.zimbra.soap.mail.message.GetRulesResponse.class,
            com.zimbra.soap.mail.message.GetSearchFolderRequest.class,
            com.zimbra.soap.mail.message.GetSearchFolderResponse.class,
            com.zimbra.soap.mail.message.GetSpellDictionariesRequest.class,
            com.zimbra.soap.mail.message.GetSpellDictionariesResponse.class,
            com.zimbra.soap.mail.message.GetTagRequest.class,
            com.zimbra.soap.mail.message.GetTagResponse.class,
            com.zimbra.soap.mail.message.GetTaskRequest.class,
            com.zimbra.soap.mail.message.GetTaskResponse.class,
            com.zimbra.soap.mail.message.GetTaskSummariesRequest.class,
            com.zimbra.soap.mail.message.GetTaskSummariesResponse.class,
            com.zimbra.soap.mail.message.GetWorkingHoursRequest.class,
            com.zimbra.soap.mail.message.GetWorkingHoursResponse.class,
            com.zimbra.soap.mail.message.GetYahooAuthTokenRequest.class,
            com.zimbra.soap.mail.message.GetYahooAuthTokenResponse.class,
            com.zimbra.soap.mail.message.GetYahooCookieRequest.class,
            com.zimbra.soap.mail.message.GetYahooCookieResponse.class,
            com.zimbra.soap.mail.message.GrantPermissionRequest.class,
            com.zimbra.soap.mail.message.GrantPermissionResponse.class,
            com.zimbra.soap.mail.message.ICalReplyRequest.class,
            com.zimbra.soap.mail.message.ICalReplyResponse.class,
            com.zimbra.soap.mail.message.ImportAppointmentsRequest.class,
            com.zimbra.soap.mail.message.ImportAppointmentsResponse.class,
            com.zimbra.soap.mail.message.ImportContactsRequest.class,
            com.zimbra.soap.mail.message.ImportContactsResponse.class,
            com.zimbra.soap.mail.message.ImportDataRequest.class,
            com.zimbra.soap.mail.message.ImportDataResponse.class,
            com.zimbra.soap.mail.message.InvalidateReminderDeviceRequest.class,
            com.zimbra.soap.mail.message.InvalidateReminderDeviceResponse.class,
            com.zimbra.soap.mail.message.ItemActionRequest.class,
            com.zimbra.soap.mail.message.ItemActionResponse.class,
            com.zimbra.soap.mail.message.ListDocumentRevisionsRequest.class,
            com.zimbra.soap.mail.message.ListDocumentRevisionsResponse.class,
            com.zimbra.soap.mail.message.ModifyAppointmentRequest.class,
            com.zimbra.soap.mail.message.ModifyAppointmentResponse.class,
            com.zimbra.soap.mail.message.ModifyContactRequest.class,
            com.zimbra.soap.mail.message.ModifyContactResponse.class,
            com.zimbra.soap.mail.message.ModifyDataSourceRequest.class,
            com.zimbra.soap.mail.message.ModifyDataSourceResponse.class,
            com.zimbra.soap.mail.message.ModifyFilterRulesRequest.class,
            com.zimbra.soap.mail.message.ModifyFilterRulesResponse.class,
            com.zimbra.soap.mail.message.ModifyMailboxMetadataRequest.class,
            com.zimbra.soap.mail.message.ModifyMailboxMetadataResponse.class,
            com.zimbra.soap.mail.message.ModifyOutgoingFilterRulesRequest.class,
            com.zimbra.soap.mail.message.ModifyOutgoingFilterRulesResponse.class,
            com.zimbra.soap.mail.message.ModifySearchFolderRequest.class,
            com.zimbra.soap.mail.message.ModifySearchFolderResponse.class,
            com.zimbra.soap.mail.message.ModifyTaskRequest.class,
            com.zimbra.soap.mail.message.ModifyTaskResponse.class,
            com.zimbra.soap.mail.message.MsgActionRequest.class,
            com.zimbra.soap.mail.message.MsgActionResponse.class,
            com.zimbra.soap.mail.message.NoOpRequest.class,
            com.zimbra.soap.mail.message.NoOpResponse.class,
            com.zimbra.soap.mail.message.NoteActionRequest.class,
            com.zimbra.soap.mail.message.NoteActionResponse.class,
            com.zimbra.soap.mail.message.PurgeRevisionRequest.class,
            com.zimbra.soap.mail.message.PurgeRevisionResponse.class,
            com.zimbra.soap.mail.message.RankingActionRequest.class,
            com.zimbra.soap.mail.message.RankingActionResponse.class,
            com.zimbra.soap.mail.message.RevokePermissionRequest.class,
            com.zimbra.soap.mail.message.RevokePermissionResponse.class,
            com.zimbra.soap.mail.message.SaveDocumentRequest.class,
            com.zimbra.soap.mail.message.SaveDocumentResponse.class,
            com.zimbra.soap.mail.message.SaveRulesRequest.class,
            com.zimbra.soap.mail.message.SaveRulesResponse.class,
            com.zimbra.soap.mail.message.SearchRequest.class,
            com.zimbra.soap.mail.message.SearchResponse.class,
            com.zimbra.soap.mail.message.SendInviteReplyRequest.class,
            com.zimbra.soap.mail.message.SendInviteReplyResponse.class,
            com.zimbra.soap.mail.message.SendVerificationCodeRequest.class,
            com.zimbra.soap.mail.message.SendVerificationCodeResponse.class,
            com.zimbra.soap.mail.message.SetAppointmentRequest.class,
            com.zimbra.soap.mail.message.SetAppointmentResponse.class,
            com.zimbra.soap.mail.message.SetCustomMetadataRequest.class,
            com.zimbra.soap.mail.message.SetCustomMetadataResponse.class,
            com.zimbra.soap.mail.message.SetMailboxMetadataRequest.class,
            com.zimbra.soap.mail.message.SetMailboxMetadataResponse.class,
            com.zimbra.soap.mail.message.SetTaskRequest.class,
            com.zimbra.soap.mail.message.SetTaskResponse.class,
            com.zimbra.soap.mail.message.SnoozeCalendarItemAlarmRequest.class,
            com.zimbra.soap.mail.message.SnoozeCalendarItemAlarmResponse.class,
            com.zimbra.soap.mail.message.SyncRequest.class,
            com.zimbra.soap.mail.message.SyncResponse.class,
            com.zimbra.soap.mail.message.TagActionRequest.class,
            com.zimbra.soap.mail.message.TagActionResponse.class,
            com.zimbra.soap.mail.message.TestDataSourceRequest.class,
            com.zimbra.soap.mail.message.TestDataSourceResponse.class,
            com.zimbra.soap.mail.message.VerifyCodeRequest.class,
            com.zimbra.soap.mail.message.VerifyCodeResponse.class,
            com.zimbra.soap.mail.message.WaitSetRequest.class,
            com.zimbra.soap.mail.message.WaitSetResponse.class,
            com.zimbra.soap.mail.message.WikiActionRequest.class,
            com.zimbra.soap.mail.message.WikiActionResponse.class,

            // zimbraAdmin
            com.zimbra.soap.admin.message.AddAccountAliasRequest.class,
            com.zimbra.soap.admin.message.AddAccountAliasResponse.class,
            com.zimbra.soap.admin.message.AddAccountLoggerRequest.class,
            com.zimbra.soap.admin.message.AddAccountLoggerResponse.class,
            com.zimbra.soap.admin.message.AddDistributionListAliasRequest.class,
            com.zimbra.soap.admin.message.AddDistributionListAliasResponse.class,
            com.zimbra.soap.admin.message.AddDistributionListMemberRequest.class,
            com.zimbra.soap.admin.message.AddDistributionListMemberResponse.class,
            com.zimbra.soap.admin.message.AdminCreateWaitSetRequest.class,
            com.zimbra.soap.admin.message.AdminCreateWaitSetResponse.class,
            com.zimbra.soap.admin.message.AdminDestroyWaitSetRequest.class,
            com.zimbra.soap.admin.message.AdminDestroyWaitSetResponse.class,
            com.zimbra.soap.admin.message.AdminWaitSetRequest.class,
            com.zimbra.soap.admin.message.AdminWaitSetResponse.class,
            com.zimbra.soap.admin.message.AuthRequest.class,
            com.zimbra.soap.admin.message.AuthResponse.class,
            com.zimbra.soap.admin.message.AutoCompleteGalRequest.class,
            com.zimbra.soap.admin.message.AutoCompleteGalResponse.class,
            com.zimbra.soap.admin.message.CheckAuthConfigRequest.class,
            com.zimbra.soap.admin.message.CheckAuthConfigResponse.class,
            com.zimbra.soap.admin.message.CheckBlobConsistencyRequest.class,
            com.zimbra.soap.admin.message.CheckBlobConsistencyResponse.class,
            com.zimbra.soap.admin.message.CheckDirectoryRequest.class,
            com.zimbra.soap.admin.message.CheckDirectoryResponse.class,
            com.zimbra.soap.admin.message.CheckDomainMXRecordRequest.class,
            com.zimbra.soap.admin.message.CheckDomainMXRecordResponse.class,
            com.zimbra.soap.admin.message.CheckExchangeAuthRequest.class,
            com.zimbra.soap.admin.message.CheckExchangeAuthResponse.class,
            com.zimbra.soap.admin.message.CheckGalConfigRequest.class,
            com.zimbra.soap.admin.message.CheckGalConfigResponse.class,
            com.zimbra.soap.admin.message.CheckHealthRequest.class,
            com.zimbra.soap.admin.message.CheckHealthResponse.class,
            com.zimbra.soap.admin.message.CheckHostnameResolveRequest.class,
            com.zimbra.soap.admin.message.CheckHostnameResolveResponse.class,
            com.zimbra.soap.admin.message.CheckPasswordStrengthRequest.class,
            com.zimbra.soap.admin.message.CheckPasswordStrengthResponse.class,
            com.zimbra.soap.admin.message.CheckRightRequest.class,
            com.zimbra.soap.admin.message.CheckRightResponse.class,
            com.zimbra.soap.admin.message.ConfigureZimletRequest.class,
            com.zimbra.soap.admin.message.ConfigureZimletResponse.class,
            com.zimbra.soap.admin.message.CopyCosRequest.class,
            com.zimbra.soap.admin.message.CopyCosResponse.class,
            com.zimbra.soap.admin.message.CountAccountRequest.class,
            com.zimbra.soap.admin.message.CountAccountResponse.class,
            com.zimbra.soap.admin.message.CreateAccountRequest.class,
            com.zimbra.soap.admin.message.CreateAccountResponse.class,
            com.zimbra.soap.admin.message.CreateCalendarResourceRequest.class,
            com.zimbra.soap.admin.message.CreateCalendarResourceResponse.class,
            com.zimbra.soap.admin.message.CreateCosRequest.class,
            com.zimbra.soap.admin.message.CreateCosResponse.class,
            com.zimbra.soap.admin.message.CreateDataSourceRequest.class,
            com.zimbra.soap.admin.message.CreateDataSourceResponse.class,
            com.zimbra.soap.admin.message.CreateDistributionListRequest.class,
            com.zimbra.soap.admin.message.CreateDistributionListResponse.class,
            com.zimbra.soap.admin.message.CreateDomainRequest.class,
            com.zimbra.soap.admin.message.CreateDomainResponse.class,
            com.zimbra.soap.admin.message.CreateGalSyncAccountRequest.class,
            com.zimbra.soap.admin.message.CreateGalSyncAccountResponse.class,
            com.zimbra.soap.admin.message.CreateServerRequest.class,
            com.zimbra.soap.admin.message.CreateServerResponse.class,
            com.zimbra.soap.admin.message.CreateVolumeRequest.class,
            com.zimbra.soap.admin.message.CreateVolumeResponse.class,
            com.zimbra.soap.admin.message.CreateXMPPComponentRequest.class,
            com.zimbra.soap.admin.message.CreateXMPPComponentResponse.class,
            com.zimbra.soap.admin.message.CreateZimletRequest.class,
            com.zimbra.soap.admin.message.CreateZimletResponse.class,
            com.zimbra.soap.admin.message.DelegateAuthRequest.class,
            com.zimbra.soap.admin.message.DelegateAuthResponse.class,
            com.zimbra.soap.admin.message.DeleteAccountRequest.class,
            com.zimbra.soap.admin.message.DeleteAccountResponse.class,
            com.zimbra.soap.admin.message.DeleteCalendarResourceRequest.class,
            com.zimbra.soap.admin.message.DeleteCalendarResourceResponse.class,
            com.zimbra.soap.admin.message.DeleteCosRequest.class,
            com.zimbra.soap.admin.message.DeleteCosResponse.class,
            com.zimbra.soap.admin.message.DeleteDataSourceRequest.class,
            com.zimbra.soap.admin.message.DeleteDataSourceResponse.class,
            com.zimbra.soap.admin.message.DeleteDistributionListRequest.class,
            com.zimbra.soap.admin.message.DeleteDistributionListResponse.class,
            com.zimbra.soap.admin.message.DeleteDomainRequest.class,
            com.zimbra.soap.admin.message.DeleteDomainResponse.class,
            com.zimbra.soap.admin.message.DeleteGalSyncAccountRequest.class,
            com.zimbra.soap.admin.message.DeleteGalSyncAccountResponse.class,
            com.zimbra.soap.admin.message.DeleteMailboxRequest.class,
            com.zimbra.soap.admin.message.DeleteMailboxResponse.class,
            com.zimbra.soap.admin.message.DeleteServerRequest.class,
            com.zimbra.soap.admin.message.DeleteServerResponse.class,
            com.zimbra.soap.admin.message.DeleteVolumeRequest.class,
            com.zimbra.soap.admin.message.DeleteVolumeResponse.class,
            com.zimbra.soap.admin.message.DeleteXMPPComponentRequest.class,
            com.zimbra.soap.admin.message.DeleteXMPPComponentResponse.class,
            com.zimbra.soap.admin.message.DeleteZimletRequest.class,
            com.zimbra.soap.admin.message.DeleteZimletResponse.class,
            com.zimbra.soap.admin.message.DeployZimletRequest.class,
            com.zimbra.soap.admin.message.DeployZimletResponse.class,
            com.zimbra.soap.admin.message.DumpSessionsRequest.class,
            com.zimbra.soap.admin.message.DumpSessionsResponse.class,
            com.zimbra.soap.admin.message.ExportAndDeleteItemsRequest.class,
            com.zimbra.soap.admin.message.ExportAndDeleteItemsResponse.class,
            com.zimbra.soap.admin.message.ExportMailboxRequest.class,
            com.zimbra.soap.admin.message.ExportMailboxResponse.class,
            com.zimbra.soap.admin.message.FixCalendarEndTimeRequest.class,
            com.zimbra.soap.admin.message.FixCalendarEndTimeResponse.class,
            com.zimbra.soap.admin.message.FixCalendarPriorityRequest.class,
            com.zimbra.soap.admin.message.FixCalendarPriorityResponse.class,
            com.zimbra.soap.admin.message.FlushCacheRequest.class,
            com.zimbra.soap.admin.message.FlushCacheResponse.class,
            com.zimbra.soap.admin.message.GetAccountInfoRequest.class,
            com.zimbra.soap.admin.message.GetAccountInfoResponse.class,
            com.zimbra.soap.admin.message.GetAccountLoggersRequest.class,
            com.zimbra.soap.admin.message.GetAccountLoggersResponse.class,
            com.zimbra.soap.admin.message.GetAccountMembershipRequest.class,
            com.zimbra.soap.admin.message.GetAccountMembershipResponse.class,
            com.zimbra.soap.admin.message.GetAccountRequest.class,
            com.zimbra.soap.admin.message.GetAccountResponse.class,
            com.zimbra.soap.admin.message.GetAdminConsoleUICompRequest.class,
            com.zimbra.soap.admin.message.GetAdminConsoleUICompResponse.class,
            com.zimbra.soap.admin.message.GetAdminSavedSearchesRequest.class,
            com.zimbra.soap.admin.message.GetAdminSavedSearchesResponse.class,
            com.zimbra.soap.admin.message.GetAllAccountLoggersRequest.class,
            com.zimbra.soap.admin.message.GetAllAccountLoggersResponse.class,
            com.zimbra.soap.admin.message.GetAllAccountsRequest.class,
            com.zimbra.soap.admin.message.GetAllAccountsResponse.class,
            com.zimbra.soap.admin.message.GetAllAdminAccountsRequest.class,
            com.zimbra.soap.admin.message.GetAllAdminAccountsResponse.class,
            com.zimbra.soap.admin.message.GetAllCalendarResourcesRequest.class,
            com.zimbra.soap.admin.message.GetAllCalendarResourcesResponse.class,
            com.zimbra.soap.admin.message.GetAllConfigRequest.class,
            com.zimbra.soap.admin.message.GetAllConfigResponse.class,
            com.zimbra.soap.admin.message.GetAllCosRequest.class,
            com.zimbra.soap.admin.message.GetAllCosResponse.class,
            com.zimbra.soap.admin.message.GetAllDistributionListsRequest.class,
            com.zimbra.soap.admin.message.GetAllDistributionListsResponse.class,
            com.zimbra.soap.admin.message.GetAllDomainsRequest.class,
            com.zimbra.soap.admin.message.GetAllDomainsResponse.class,
            com.zimbra.soap.admin.message.GetAllEffectiveRightsRequest.class,
            com.zimbra.soap.admin.message.GetAllEffectiveRightsResponse.class,
            com.zimbra.soap.admin.message.GetAllFreeBusyProvidersRequest.class,
            com.zimbra.soap.admin.message.GetAllFreeBusyProvidersResponse.class,
            com.zimbra.soap.admin.message.GetAllLocalesRequest.class,
            com.zimbra.soap.admin.message.GetAllLocalesResponse.class,
            com.zimbra.soap.admin.message.GetAllMailboxesRequest.class,
            com.zimbra.soap.admin.message.GetAllMailboxesResponse.class,
            com.zimbra.soap.admin.message.GetAllRightsRequest.class,
            com.zimbra.soap.admin.message.GetAllRightsResponse.class,
            com.zimbra.soap.admin.message.GetAllServersRequest.class,
            com.zimbra.soap.admin.message.GetAllServersResponse.class,
            com.zimbra.soap.admin.message.GetAllVolumesRequest.class,
            com.zimbra.soap.admin.message.GetAllVolumesResponse.class,
            com.zimbra.soap.admin.message.GetAllXMPPComponentsRequest.class,
            com.zimbra.soap.admin.message.GetAllXMPPComponentsResponse.class,
            com.zimbra.soap.admin.message.GetAllZimletsRequest.class,
            com.zimbra.soap.admin.message.GetAllZimletsResponse.class,
            com.zimbra.soap.admin.message.GetCalendarResourceRequest.class,
            com.zimbra.soap.admin.message.GetCalendarResourceResponse.class,
            com.zimbra.soap.admin.message.GetConfigRequest.class,
            com.zimbra.soap.admin.message.GetConfigResponse.class,
            com.zimbra.soap.admin.message.GetCosRequest.class,
            com.zimbra.soap.admin.message.GetCosResponse.class,
            com.zimbra.soap.admin.message.GetCreateObjectAttrsRequest.class,
            com.zimbra.soap.admin.message.GetCreateObjectAttrsResponse.class,
            com.zimbra.soap.admin.message.GetCurrentVolumesRequest.class,
            com.zimbra.soap.admin.message.GetCurrentVolumesResponse.class,
            com.zimbra.soap.admin.message.GetDataSourcesRequest.class,
            com.zimbra.soap.admin.message.GetDataSourcesResponse.class,
            com.zimbra.soap.admin.message.GetDelegatedAdminConstraintsRequest.class,
            com.zimbra.soap.admin.message.GetDelegatedAdminConstraintsResponse.class,
            com.zimbra.soap.admin.message.GetDistributionListMembershipRequest.class,
            com.zimbra.soap.admin.message.GetDistributionListMembershipResponse.class,
            com.zimbra.soap.admin.message.GetDistributionListRequest.class,
            com.zimbra.soap.admin.message.GetDistributionListResponse.class,
            com.zimbra.soap.admin.message.GetDomainInfoRequest.class,
            com.zimbra.soap.admin.message.GetDomainInfoResponse.class,
            com.zimbra.soap.admin.message.GetDomainRequest.class,
            com.zimbra.soap.admin.message.GetDomainResponse.class,
            com.zimbra.soap.admin.message.GetEffectiveRightsRequest.class,
            com.zimbra.soap.admin.message.GetEffectiveRightsResponse.class,
            com.zimbra.soap.admin.message.GetFreeBusyQueueInfoRequest.class,
            com.zimbra.soap.admin.message.GetFreeBusyQueueInfoResponse.class,
            com.zimbra.soap.admin.message.GetGrantsRequest.class,
            com.zimbra.soap.admin.message.GetGrantsResponse.class,
            com.zimbra.soap.admin.message.GetLicenseInfoRequest.class,
            com.zimbra.soap.admin.message.GetLicenseInfoResponse.class,
            com.zimbra.soap.admin.message.GetLoggerStatsRequest.class,
            com.zimbra.soap.admin.message.GetLoggerStatsResponse.class,
            com.zimbra.soap.admin.message.GetMailQueueInfoRequest.class,
            com.zimbra.soap.admin.message.GetMailQueueInfoResponse.class,
            com.zimbra.soap.admin.message.GetMailQueueRequest.class,
            com.zimbra.soap.admin.message.GetMailQueueResponse.class,
            com.zimbra.soap.admin.message.GetMailboxRequest.class,
            com.zimbra.soap.admin.message.GetMailboxResponse.class,
            com.zimbra.soap.admin.message.GetMailboxStatsRequest.class,
            com.zimbra.soap.admin.message.GetMailboxStatsResponse.class,
            com.zimbra.soap.admin.message.GetMemcachedClientConfigRequest.class,
            com.zimbra.soap.admin.message.GetMemcachedClientConfigResponse.class,
            com.zimbra.soap.admin.message.GetPublishedShareInfoRequest.class,
            com.zimbra.soap.admin.message.GetPublishedShareInfoResponse.class,
            com.zimbra.soap.admin.message.GetQuotaUsageRequest.class,
            com.zimbra.soap.admin.message.GetQuotaUsageResponse.class,
            com.zimbra.soap.admin.message.GetRightRequest.class,
            com.zimbra.soap.admin.message.GetRightResponse.class,
            com.zimbra.soap.admin.message.GetRightsDocRequest.class,
            com.zimbra.soap.admin.message.GetRightsDocResponse.class,
            com.zimbra.soap.admin.message.GetSMIMEConfigRequest.class,
            com.zimbra.soap.admin.message.GetSMIMEConfigResponse.class,
            com.zimbra.soap.admin.message.GetServerNIfsRequest.class,
            com.zimbra.soap.admin.message.GetServerNIfsResponse.class,
            com.zimbra.soap.admin.message.GetServerRequest.class,
            com.zimbra.soap.admin.message.GetServerResponse.class,
            com.zimbra.soap.admin.message.GetServerStatsRequest.class,
            com.zimbra.soap.admin.message.GetServerStatsResponse.class,
            com.zimbra.soap.admin.message.GetServiceStatusRequest.class,
            com.zimbra.soap.admin.message.GetServiceStatusResponse.class,
            com.zimbra.soap.admin.message.GetSessionsRequest.class,
            com.zimbra.soap.admin.message.GetSessionsResponse.class,
            com.zimbra.soap.admin.message.GetShareInfoRequest.class,
            com.zimbra.soap.admin.message.GetShareInfoResponse.class,
            com.zimbra.soap.admin.message.GetVersionInfoRequest.class,
            com.zimbra.soap.admin.message.GetVersionInfoResponse.class,
            com.zimbra.soap.admin.message.GetVolumeRequest.class,
            com.zimbra.soap.admin.message.GetVolumeResponse.class,
            com.zimbra.soap.admin.message.GetXMPPComponentRequest.class,
            com.zimbra.soap.admin.message.GetXMPPComponentResponse.class,
            com.zimbra.soap.admin.message.GetZimletRequest.class,
            com.zimbra.soap.admin.message.GetZimletResponse.class,
            com.zimbra.soap.admin.message.GetZimletStatusRequest.class,
            com.zimbra.soap.admin.message.GetZimletStatusResponse.class,
            com.zimbra.soap.admin.message.GrantRightRequest.class,
            com.zimbra.soap.admin.message.GrantRightResponse.class,
            com.zimbra.soap.admin.message.MailQueueActionRequest.class,
            com.zimbra.soap.admin.message.MailQueueActionResponse.class,
            com.zimbra.soap.admin.message.MailQueueFlushRequest.class,
            com.zimbra.soap.admin.message.MailQueueFlushResponse.class,
            com.zimbra.soap.admin.message.MigrateAccountRequest.class,
            com.zimbra.soap.admin.message.MigrateAccountResponse.class,
            com.zimbra.soap.admin.message.ModifyAccountRequest.class,
            com.zimbra.soap.admin.message.ModifyAccountResponse.class,
            com.zimbra.soap.admin.message.ModifyAdminSavedSearchesRequest.class,
            com.zimbra.soap.admin.message.ModifyAdminSavedSearchesResponse.class,
            com.zimbra.soap.admin.message.ModifyCalendarResourceRequest.class,
            com.zimbra.soap.admin.message.ModifyCalendarResourceResponse.class,
            com.zimbra.soap.admin.message.ModifyConfigRequest.class,
            com.zimbra.soap.admin.message.ModifyConfigResponse.class,
            com.zimbra.soap.admin.message.ModifyCosRequest.class,
            com.zimbra.soap.admin.message.ModifyCosResponse.class,
            com.zimbra.soap.admin.message.ModifyDataSourceRequest.class,
            com.zimbra.soap.admin.message.ModifyDataSourceResponse.class,
            com.zimbra.soap.admin.message.ModifyDelegatedAdminConstraintsRequest.class,
            com.zimbra.soap.admin.message.ModifyDelegatedAdminConstraintsResponse.class,
            com.zimbra.soap.admin.message.ModifyDistributionListRequest.class,
            com.zimbra.soap.admin.message.ModifyDistributionListResponse.class,
            com.zimbra.soap.admin.message.ModifyDomainRequest.class,
            com.zimbra.soap.admin.message.ModifyDomainResponse.class,
            com.zimbra.soap.admin.message.ModifySMIMEConfigRequest.class,
            com.zimbra.soap.admin.message.ModifySMIMEConfigResponse.class,
            com.zimbra.soap.admin.message.ModifyServerRequest.class,
            com.zimbra.soap.admin.message.ModifyServerResponse.class,
            com.zimbra.soap.admin.message.ModifyVolumeRequest.class,
            com.zimbra.soap.admin.message.ModifyVolumeResponse.class,
            com.zimbra.soap.admin.message.ModifyZimletRequest.class,
            com.zimbra.soap.admin.message.ModifyZimletResponse.class,
            com.zimbra.soap.admin.message.NoOpRequest.class,
            com.zimbra.soap.admin.message.NoOpResponse.class,
            com.zimbra.soap.admin.message.PingRequest.class,
            com.zimbra.soap.admin.message.PingResponse.class,
            com.zimbra.soap.admin.message.PublishShareInfoRequest.class,
            com.zimbra.soap.admin.message.PublishShareInfoResponse.class,
            com.zimbra.soap.admin.message.PurgeAccountCalendarCacheRequest.class,
            com.zimbra.soap.admin.message.PurgeAccountCalendarCacheResponse.class,
            com.zimbra.soap.admin.message.PurgeFreeBusyQueueRequest.class,
            com.zimbra.soap.admin.message.PurgeFreeBusyQueueResponse.class,
            com.zimbra.soap.admin.message.PurgeMessagesRequest.class,
            com.zimbra.soap.admin.message.PurgeMessagesResponse.class,
            com.zimbra.soap.admin.message.PushFreeBusyRequest.class,
            com.zimbra.soap.admin.message.PushFreeBusyResponse.class,
            com.zimbra.soap.admin.message.QueryWaitSetRequest.class,
            com.zimbra.soap.admin.message.QueryWaitSetResponse.class,
            com.zimbra.soap.admin.message.ReIndexRequest.class,
            com.zimbra.soap.admin.message.ReIndexResponse.class,
            com.zimbra.soap.admin.message.RecalculateMailboxCountsRequest.class,
            com.zimbra.soap.admin.message.RecalculateMailboxCountsResponse.class,
            com.zimbra.soap.admin.message.ReloadLocalConfigRequest.class,
            com.zimbra.soap.admin.message.ReloadLocalConfigResponse.class,
            com.zimbra.soap.admin.message.ReloadMemcachedClientConfigRequest.class,
            com.zimbra.soap.admin.message.ReloadMemcachedClientConfigResponse.class,
            com.zimbra.soap.admin.message.RemoveAccountAliasRequest.class,
            com.zimbra.soap.admin.message.RemoveAccountAliasResponse.class,
            com.zimbra.soap.admin.message.RemoveAccountLoggerRequest.class,
            com.zimbra.soap.admin.message.RemoveAccountLoggerResponse.class,
            com.zimbra.soap.admin.message.RemoveDistributionListAliasRequest.class,
            com.zimbra.soap.admin.message.RemoveDistributionListAliasResponse.class,
            com.zimbra.soap.admin.message.RemoveDistributionListMemberRequest.class,
            com.zimbra.soap.admin.message.RemoveDistributionListMemberResponse.class,
            com.zimbra.soap.admin.message.RenameAccountRequest.class,
            com.zimbra.soap.admin.message.RenameAccountResponse.class,
            com.zimbra.soap.admin.message.RenameCalendarResourceRequest.class,
            com.zimbra.soap.admin.message.RenameCalendarResourceResponse.class,
            com.zimbra.soap.admin.message.RenameCosRequest.class,
            com.zimbra.soap.admin.message.RenameCosResponse.class,
            com.zimbra.soap.admin.message.RenameDistributionListRequest.class,
            com.zimbra.soap.admin.message.RenameDistributionListResponse.class,
            com.zimbra.soap.admin.message.ResetAllLoggersRequest.class,
            com.zimbra.soap.admin.message.ResetAllLoggersResponse.class,
            com.zimbra.soap.admin.message.RevokeRightRequest.class,
            com.zimbra.soap.admin.message.RevokeRightResponse.class,
            com.zimbra.soap.admin.message.RunUnitTestsRequest.class,
            com.zimbra.soap.admin.message.RunUnitTestsResponse.class,
            com.zimbra.soap.admin.message.SearchAccountsRequest.class,
            com.zimbra.soap.admin.message.SearchAccountsResponse.class,
            com.zimbra.soap.admin.message.SearchCalendarResourcesRequest.class,
            com.zimbra.soap.admin.message.SearchCalendarResourcesResponse.class,
            com.zimbra.soap.admin.message.SearchDirectoryRequest.class,
            com.zimbra.soap.admin.message.SearchDirectoryResponse.class,
            com.zimbra.soap.admin.message.SetCurrentVolumeRequest.class,
            com.zimbra.soap.admin.message.SetCurrentVolumeResponse.class,
            com.zimbra.soap.admin.message.SetPasswordRequest.class,
            com.zimbra.soap.admin.message.SetPasswordResponse.class,
            com.zimbra.soap.admin.message.UndeployZimletRequest.class,
            com.zimbra.soap.admin.message.UndeployZimletResponse.class,
            com.zimbra.soap.admin.message.VerifyIndexRequest.class,
            com.zimbra.soap.admin.message.VerifyIndexResponse.class
        };

        try {
            JAXB_CONTEXT = JAXBContext.newInstance(MESSAGE_CLASSES);
        } catch (JAXBException e) {
            LOG.error("Unable to initialize JAXB", e);
        }
    }

    private JaxbUtil() {
    }

    public static Class<?>[] getJaxbRequestAndResponseClasses() {
        return MESSAGE_CLASSES;
    }

    /**
     * @param o - associated class must have an @XmlRootElement annotation
     * @param factory - e.g. XmlElement.mFactory or JSONElement.mFactory
     * @return
     * @throws ServiceException
     */
    public static Element jaxbToElement(Object o, Element.ElementFactory factory)
    throws ServiceException {
        try {
            Marshaller marshaller = getContext().createMarshaller();
            // marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            DocumentResult dr = new DocumentResult();
            marshaller.marshal(o, dr);
            Document theDoc = dr.getDocument();
            org.dom4j.Element rootElem = theDoc.getRootElement();
            return Element.convertDOM(rootElem, factory);
        } catch (Exception e) {
            throw ServiceException.FAILURE("Unable to convert " +
                    o.getClass().getName() + " to Element", e);
        }
    }

    public static Element jaxbToElement(Object o)
    throws ServiceException {
        return jaxbToElement(o, XMLElement.mFactory);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Element jaxbToNamedElement(String name,
            String namespace, Object o, Element.ElementFactory factory)
    throws ServiceException {
        try {
            JAXBContext jaxb = JAXBContext.newInstance(o.getClass());
            Marshaller marshaller = jaxb.createMarshaller();
            // marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            DocumentResult dr = new DocumentResult();
            marshaller.marshal(new JAXBElement(
                    new QName(namespace, name), o.getClass(), o) , dr);
            Document theDoc = dr.getDocument();
            org.dom4j.Element rootElem = theDoc.getRootElement();
            return Element.convertDOM(rootElem, factory);
        } catch (Exception e) {
            throw ServiceException.FAILURE("Unable to convert " +
                    o.getClass().getName() + " to Element", e);
        }
    }

    public static Element addChildElementFromJaxb(Element parent,
            String name, String namespace, Object o) {
        Element.ElementFactory factory;
        if (parent instanceof XMLElement)
            factory = XMLElement.mFactory;
        else
            factory = JSONElement.mFactory;
        Element child = null;
        try {
            child = jaxbToNamedElement(name, namespace, o, factory);
        } catch (ServiceException e) {
            ZimbraLog.misc.info("JAXB Problem making " + name + " element", e);
        }
        parent.addElement(child);
        return child;
    }

    /**
     * This appears to be safe but is fairly slow.
     * Note that this method does NOT support Zimbra's greater flexibility
     * for Xml structure.  Something similar to {@link fixupStructureForJaxb}
     * will be needed to add such support.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> T elementToJaxbUsingByteArray(Element e)
    throws ServiceException {
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();
            org.dom4j.Element rootElem = e.toXML();
            return (T) unmarshaller.unmarshal(new ByteArrayInputStream(
                        rootElem.asXML().getBytes("utf-8")));
        } catch (JAXBException ex) {
            throw ServiceException.FAILURE(
                    "Unable to unmarshal response for " + e.getName(), ex);
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(
                    "Unable to unmarshal response for " + e.getName(), ex);
        }
    }

    /**
     * This appears to work if e is an XMLElement but sometimes fails badly if
     * e is a JSONElement - get:
     * javax.xml.bind.UnmarshalException: Namespace URIs and local names
     *      to the unmarshaller needs to be interned.
     * and that seems to make the unmarshaller unstable from then on :-(
     * Note that this method does NOT support Zimbra's greater flexibility
     * for Xml structure.  Something similar to {@link fixupStructureForJaxb}
     * will be needed to add such support.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> T elementToJaxbUsingDom4j(Element e)
    throws ServiceException {
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();
            org.dom4j.Element rootElem = e.toXML();
            DocumentSource docSrc = new DocumentSource(rootElem);
            return (T) unmarshaller.unmarshal(docSrc);
        } catch (JAXBException ex) {
            throw ServiceException.FAILURE(
                    "Unable to unmarshal response for " + e.getName(), ex);
        }
    }

    /**
     * Manipulates a structure under {@link elem} which obeys Zimbra's
     * SOAP XML structure rules to comply with more stringent JAXB rules.
     * e.g. Zimbra allows attributes to be specified as elements.
     * @param klass is the JAXB class for {@link elem}
     */
    public static void fixupStructureForJaxb(org.w3c.dom.Element elem,
                    Class<?> klass) {
        if (elem == null) {
            return;
        }
        if (klass == null) {
            LOG.debug("JAXB no class associated with " + elem.getLocalName());
            return;
        }
        if (!klass.getName().startsWith("com.zimbra.soap")) {
            return;
        }

        JaxbInfo jaxbInfo = JaxbInfo.getFromCache(klass);
        if (jaxbInfo == null) {
            jaxbInfo = new JaxbInfo(klass);
        }
        NodeList list = elem.getChildNodes();
        for (int i=0; i < list.getLength(); i++) {
            Node subnode = list.item(i);
            if (subnode.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element child = (org.w3c.dom.Element) subnode;
                String childName = child.getLocalName();
                if (jaxbInfo.hasWrappedElement(childName)) {
                    NodeList wrappedList = child.getChildNodes();
                    for (int j=0; j < wrappedList.getLength(); j++) {
                        Node wSubnode = wrappedList.item(j);
                        if (wSubnode.getNodeType() == Node.ELEMENT_NODE) {
                            org.w3c.dom.Element wChild =
                                (org.w3c.dom.Element) wSubnode;
                            fixupStructureForJaxb(wChild,
                                    jaxbInfo.getClassForWrappedElement(
                                            childName, wChild.getLocalName()));
                        }
                    }
                } else if (jaxbInfo.hasElement(childName))  {
                    fixupStructureForJaxb(child,
                            jaxbInfo.getClassForElement(childName));
                } else if (jaxbInfo.hasAttribute(childName)) {
                    // TODO: remove logging
                    LOG.debug("Promoting element '" + childName +
                            "' to attribute for JAXB class " + klass.getName());
                    elem.setAttribute(childName, child.getTextContent());
                    // Don't remove pre-existing child until later pass
                    // to avoid changing the list of child elements
                } else {
                    LOG.debug("JAXB class " + klass.getName() +
                            " does NOT recognise element named:" + childName);
                }
            }
        }
        // Prune the promoted elements from the list of children
        list = elem.getChildNodes();
        for (int i=0; i < list.getLength(); i++) {
            Node subnode = list.item(i);
            if (subnode.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element child = (org.w3c.dom.Element) subnode;
                String childName = child.getLocalName();
                if  (   (!jaxbInfo.hasWrappedElement(childName)) &&
                        (!jaxbInfo.hasElement(childName)) &&
                        (jaxbInfo.hasAttribute(childName))) {
                    elem.removeChild(child);
                }
            }
        }
    }

    /**
     * Manipulates a structure under {@link elem} which obeys Zimbra's
     * SOAP XML structure rules to comply with more stringent JAXB rules.
     * e.g. Zimbra allows attributes to be specified as elements.
     * The JAXB class associated with this element MUST be discoverable
     * from {@link elem} based on it's namespace and localname.
     */
    public static void fixupStructureForJaxb(org.w3c.dom.Element elem) {
        String className = null;
        try {
            String ns = elem.getNamespaceURI();
            if (AdminConstants.NAMESPACE_STR.equals(ns)) {
                className = ADMIN_JAXB_PACKAGE  + "." + elem.getLocalName();
            } else if (AccountConstants.NAMESPACE_STR.equals(ns)) {
                className = ACCOUNT_JAXB_PACKAGE  + "." + elem.getLocalName();
            } else if (MailConstants.NAMESPACE_STR.equals(ns)) {
                className = MAIL_JAXB_PACKAGE  + "." + elem.getLocalName();
            } else {
                LOG.info("Unexpected namespace[" + ns + "]");
                return;
            }
            Class<?> klass = Class.forName(className);
            if (klass == null) {
                LOG.info("Failed to find CLASS for name=[" + className + "]");
                return;
            }
            fixupStructureForJaxb(elem, klass);
            // JaxbInfo.clearCache();
        } catch (NullPointerException npe) {
            LOG.info("Problem finding JAXB package", npe);
            return;
        } catch (ClassNotFoundException cnfe) {
            LOG.info("Problem finding JAXB class", cnfe);
            return;
        }
    }

    /**
     * @param elem represents a structure which may only match Zimbra's more
     * relaxed rules rather than stringent JAXB rules.
     * e.g. Zimbra allows attributes to be specified as elements.
     * @param klass is the JAXB class for {@link elem}
     * @return a JAXB object
     */
    @SuppressWarnings("unchecked")
    public static <T> T w3cDomDocToJaxb(org.w3c.dom.Document doc)
    throws ServiceException {
        fixupStructureForJaxb((org.w3c.dom.Element)doc.getDocumentElement());
        return (T) rawW3cDomDocToJaxb(doc);
    }

    /**
     * Return a JAXB object.  This implementation uses a org.w3c.dom.Document
     * as an intermediate representation.  This appears to be more reliable
     * than using a DocumentSource based on org.dom4j.Element
     */
    @SuppressWarnings("unchecked")
    private static <T> T rawW3cDomDocToJaxb(org.w3c.dom.Document doc)
    throws ServiceException {
        if ((doc == null || doc.getDocumentElement() == null)) {
            return null;
        }
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();
            // LOG.warn("Dom to Xml:\n" + domToString(doc));
            return (T) unmarshaller.unmarshal(doc);
        } catch (JAXBException ex) {
            throw ServiceException.FAILURE(
                    "Unable to unmarshal response for " +
                    doc.getDocumentElement().getNodeName(), ex);
        }
    }

    /**
     * Return a JAXB object.  This implementation uses a org.w3c.dom.Document
     * as an intermediate representation.  This appears to be more reliable
     * than using a DocumentSource based on org.dom4j.Element
     */
    @SuppressWarnings("unchecked")
    public static <T> T elementToJaxb(Element e)
    throws ServiceException {
        return (T) w3cDomDocToJaxb(e.toW3cDom());
    }

    public static String domToString(org.w3c.dom.Document document) {
        try {
            Source xmlSource = new DOMSource(document);
            StreamResult result = new StreamResult(new ByteArrayOutputStream());
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("indent", "yes"); //Java XML Indent
            transformer.transform(xmlSource, result);
            return result.getOutputStream().toString();
        } catch (TransformerFactoryConfigurationError factoryError) {
            LOG.error("Error creating TransformerFactory", factoryError);
        } catch (TransformerException transformerError) {
            LOG.error( "Error transforming document", transformerError);
        }
        return null;
    }

    private static JAXBContext getContext()
    throws ServiceException {
        if (JAXB_CONTEXT == null) {
            throw ServiceException.FAILURE("JAXB has not been initialized", null);
        }
        return JAXB_CONTEXT;
    }
}

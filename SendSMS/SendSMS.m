//
//  SendSMS.m
//  SendSMS
//
//  Created by Trevor Porter on 7/13/16.


#import "SendSMS.h"
#import "RCTUtils.h"

@implementation SendSMS

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(send:(NSDictionary *)options
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    _resolve = resolve;
    _reject = reject;
    MFMessageComposeViewController *messageController = [[MFMessageComposeViewController alloc] init];
    if([MFMessageComposeViewController canSendText])
    {

        NSString *body = options[@"body"];
        NSArray *recipients = options[@"recipients"];

        if (body) {
          messageController.body = body;
        }

        if (recipients) {
          messageController.recipients = recipients;
        }

        messageController.messageComposeDelegate = self;
        UIViewController *currentViewController = [UIApplication sharedApplication].keyWindow.rootViewController;
        while(currentViewController.presentedViewController) {
            currentViewController = currentViewController.presentedViewController;
        }
        [currentViewController presentViewController:messageController animated:YES completion:nil];
    } else {
        _reject(@"error", @"error", nil);
    }
}

-(void) messageComposeViewController:(MFMessageComposeViewController *)controller didFinishWithResult:(MessageComposeResult)result {
    switch (result) {
        case MessageComposeResultSent:
            _resolve(@YES);
            break;
        case MessageComposeResultCancelled:
            _reject(@"cancelled", @"cancelled", nil);
            break;
        default:
            _reject(@"error", @"error", nil);
            break;
    }

    [controller dismissViewControllerAnimated:YES completion:nil];
}

@end

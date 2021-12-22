//
//  NavigationViewController.swift
//  reactNativeNavigationIntegration
//
//  Created by Emre Kuru on 21.10.2021.
//

import UIKit
import PoilabsNavigation

class NavigationViewController: UIViewController {

  var currentCarrier: PLNNavigationMapView?
  @IBOutlet weak var navigationView: UIView!
  
    override func viewDidLoad() {
        super.viewDidLoad()

      PLNNavigationSettings.sharedInstance().mallId = "PLACE_TITLE"
      PLNNavigationSettings.sharedInstance().applicationId = "ae009a0f-524f-45bb-ae21-ba07914c4f16"
      PLNNavigationSettings.sharedInstance().applicationSecret = "ae009a0f-524f-45bb-ae21-ba07914c4f16"

      PLNavigationManager.sharedInstance()?.getReadyForStoreMap(completionHandler: { (error) in
        if error == nil {
            let carrierView = PLNNavigationMapView(frame: CGRect(x: 0, y: 0, width: self.navigationView.bounds.size.width, height: self.navigationView.bounds.size.height))
            carrierView.awakeFromNib()
            carrierView.delegate = self
            carrierView.searchBarBaseView.backgroundColor = UIColor.black
            carrierView.searchCancelButton.setTitleColor(.white, for: .normal)
            self.currentCarrier = carrierView
            self.navigationView.addSubview(carrierView)
          } else {
            //show error
          }
      })
    }
  
  @IBAction func dismissNavigation(_ sender: Any) {
    self.dismiss(animated: true, completion: nil)
  }
  
}

extension NavigationViewController: PLNNavigationMapViewDelegate {
  func childsAreReady() {
    print("childs are ready")
  }
}

//
//    ContactProxy2.java is part of SyncMyPix
//
//    Authors:
//        Neil Loknath <neil.loknath@gmail.com>
//
//    Copyright (c) 2009 Neil Loknath
//
//    SyncMyPix is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    SyncMyPix is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with SyncMyPix.  If not, see <http://www.gnu.org/licenses/>.
//

package com.nloko.android.syncmypix.contactutils;

import java.io.InputStream;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;

public class ContactProxy2 extends ContactProxy {

	@Override
	public InputStream getPhoto(ContentResolver cr, String id) {
		if (cr == null || id == null) {
			return null;
		}
		
		Uri contact = Uri.withAppendedPath(Contacts.CONTENT_URI, id);
		return Contacts.openContactPhotoInputStream(cr, contact);
	}
}

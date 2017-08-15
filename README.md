# valuesauce

## Create the keystore:

srupik@debian:~$ keytool -keystore valuesauce.jks -genkey
Enter keystore password:  <keystore_password>
Re-enter new password: <keystore_password>
What is your first and last name?
  [Unknown]:  Seb Rupik
What is the name of your organizational unit?
  [Unknown]:  IT
What is the name of your organization?
  [Unknown]:  Foo
What is the name of your City or Locality?
  [Unknown]:  Bar
What is the name of your State or Province?
  [Unknown]:  Foobar
What is the two-letter country code for this unit?
  [Unknown]:  GB
Is CN=Seb Rupik, OU=IT, O=Foo, L=Bar, ST=Foobar, C=GB correct?
  [no]:  yes

Enter key password for <mykey>
	(RETURN if same as keystore password):  <key_password>
Re-enter new password: <key_password>
srupik@debian:~/NetBeansProjects/valuesauce$

## Export the public key

srupik@debian:~$ keytool -exportcert -file valuesauce_public.crt -keystore valuesauce.jks -storepass <keystore_password>
Certificate stored in file <valuesauce_public.crt>
srupik@debian:~$ keytool -importcert -file valuesauce_public.crt -keystore valuesauce_public.jks -storepass <keystore_password>
Owner: CN=Seb Rupik, OU=IT, O=Foo, L=Bar, ST=Foobar, C=GB
Issuer: CN=Seb Rupik, OU=IT, O=Foo, L=Bar, ST=Foobar, C=GB
Serial number: 68ef80ae
Valid from: Tue Aug 15 10:39:48 BST 2017 until: Mon Nov 13 09:39:48 GMT 2017
Certificate fingerprints:
	 MD5:  F4:0F:2C:E6:2A:7F:D2:9F:74:46:F7:06:16:D6:F5:9B
	 SHA1: AE:0D:21:C9:27:98:1F:A6:EF:D6:FD:C0:4F:66:C8:4B:A7:AE:29:2B
	 SHA256: 13:22:78:83:6B:B5:9A:7A:50:77:CA:EC:B1:3E:65:04:65:1F:1E:10:24:1D:CE:0E:80:8E:A3:D3:A6:5C:4E:DA
	 Signature algorithm name: SHA1withDSA
	 Version: 3

Extensions: 

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: 7F 8D BA 76 BB EC A7 96   6B F8 E1 FA 32 31 E1 4D  ...v....k...21.M
0010: E5 F6 E2 A2                                        ....
]
]

Trust this certificate? [no]:  yes
Certificate was added to keystore
srupik@debian:~$


## Running valuesauce

java -jar dist/valuesauce.jar ~/valuesauce.jks <keytore_password> <key_password>

## Running sleetlocust

srupik@debian:~$ java -Djavax.net.ssl.trustStore=valuesauce_public.jks -jar dist/sleetlocust.jar


load -ascii mask.txt
smask = zeros(95,95);

smask(49:95,49:95) = mask(1:47,1:47);
smask(49:95,1:47) = fliplr(mask(1:47,1:47));
smask(1:47,49:95) = flipud(mask(1:47,1:47));
smask(1:47,1:47) = fliplr(flipud(mask(1:47,1:47)));
smask(48,48)=1;
smask(:,48)=0;
smask(48,:)=1;
smask = 2*smask-1;
x=[1:231];
y=[1:231];

x = x/2.4316;
y = y/2.4316;
xx = uint16(floor(x)+1);
yy = uint16(floor(y)+1);

tmask = zeros(400,400);

tmask(87:317,87:317)=smask(xx,yy);

pmask=tmask>0.1;
nmask=tmask<-0.1;

fpmask = fft2(pmask);
fnmask = fft2(nmask);
